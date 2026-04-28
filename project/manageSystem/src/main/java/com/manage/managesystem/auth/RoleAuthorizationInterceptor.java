package com.manage.managesystem.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manage.managesystem.audit.AuditLogContext;
import com.manage.managesystem.common.response.ApiResponse;
import com.manage.managesystem.enums.SystemRoleEnum;
import com.manage.managesystem.util.RoleCodeUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class RoleAuthorizationInterceptor implements HandlerInterceptor {
    private static final String UNAUTHORIZED_MESSAGE = "\u672A\u767B\u5F55\u6216\u767B\u5F55\u5DF2\u8FC7\u671F";
    private static final String ROLE_MISSING_MESSAGE = "\u5F53\u524D\u7528\u6237\u672A\u5206\u914D\u89D2\u8272";
    private static final String FORBIDDEN_MESSAGE = "\u6CA1\u6709\u8BBF\u95EE\u6743\u9650";

    private final ObjectMapper objectMapper;

    public RoleAuthorizationInterceptor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return true;
        }

        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (requireRole == null) {
            requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }
        if (requireRole == null) {
            return true;
        }

        AuthUser authUser = UserContextHolder.get();
        if (authUser == null) {
            request.setAttribute(AuditLogContext.RESULT_CODE_ATTR, HttpServletResponse.SC_UNAUTHORIZED);
            writeForbidden(response, HttpServletResponse.SC_UNAUTHORIZED, UNAUTHORIZED_MESSAGE);
            return false;
        }

        List<String> roleCodes = authUser.getRoleCodes();
        if (roleCodes == null || roleCodes.isEmpty()) {
            request.setAttribute(AuditLogContext.RESULT_CODE_ATTR, HttpServletResponse.SC_FORBIDDEN);
            writeForbidden(response, HttpServletResponse.SC_FORBIDDEN, ROLE_MISSING_MESSAGE);
            return false;
        }

        boolean allowed = Arrays.stream(requireRole.value())
                .map(SystemRoleEnum::name)
                .anyMatch(roleCodes::contains);
        if (!allowed && RoleCodeUtils.hasBusinessUserRole(roleCodes)) {
            allowed = Arrays.stream(requireRole.value())
                    .map(SystemRoleEnum::name)
                    .anyMatch(code -> !SystemRoleEnum.SYS_ADMIN.name().equals(code));
        }
        if (!allowed) {
            request.setAttribute(AuditLogContext.RESULT_CODE_ATTR, HttpServletResponse.SC_FORBIDDEN);
            writeForbidden(response, HttpServletResponse.SC_FORBIDDEN, FORBIDDEN_MESSAGE);
            return false;
        }

        return true;
    }

    private void writeForbidden(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.fail(status, message)));
    }
}

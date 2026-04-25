package com.manage.managesystem.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manage.managesystem.common.response.ApiResponse;
import com.manage.managesystem.enums.SystemRoleEnum;
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
            writeForbidden(response, HttpServletResponse.SC_UNAUTHORIZED, "未登录或登录已过期");
            return false;
        }

        List<String> roleCodes = authUser.getRoleCodes();
        if (roleCodes == null || roleCodes.isEmpty()) {
            writeForbidden(response, HttpServletResponse.SC_FORBIDDEN, "当前用户未分配角色");
            return false;
        }

        boolean allowed = Arrays.stream(requireRole.value())
                .map(SystemRoleEnum::name)
                .anyMatch(roleCodes::contains);
        if (!allowed) {
            writeForbidden(response, HttpServletResponse.SC_FORBIDDEN, "没有访问权限");
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

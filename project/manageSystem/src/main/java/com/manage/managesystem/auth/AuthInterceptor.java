package com.manage.managesystem.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manage.managesystem.audit.AuditLogContext;
import com.manage.managesystem.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private static final String UNAUTHORIZED_MESSAGE = "\u672A\u767B\u5F55\u6216\u767B\u5F55\u5DF2\u8FC7\u671F";

    private final TokenService tokenService;
    private final ObjectMapper objectMapper;

    public AuthInterceptor(TokenService tokenService, ObjectMapper objectMapper) {
        this.tokenService = tokenService;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return true;
        }

        String token = resolveToken(request);
        TokenSession session = tokenService.getValidSession(token);
        if (session == null) {
            request.setAttribute(AuditLogContext.RESULT_CODE_ATTR, HttpServletResponse.SC_UNAUTHORIZED);
            writeUnauthorized(response, HttpServletResponse.SC_UNAUTHORIZED, UNAUTHORIZED_MESSAGE);
            return false;
        }

        request.setAttribute(AuditLogContext.OPERATOR_ID_ATTR, session.getUserId());
        request.setAttribute(
                AuditLogContext.OPERATOR_NAME_ATTR,
                session.getRealName() != null && !session.getRealName().isBlank() ? session.getRealName() : session.getUsername()
        );
        UserContextHolder.set(AuthUser.builder()
                .userId(session.getUserId())
                .username(session.getUsername())
                .realName(session.getRealName())
                .email(session.getEmail())
                .phone(session.getPhone())
                .avatarUrl(session.getAvatarUrl())
                .status(session.getStatus())
                .roleCodes(session.getRoleCodes())
                .token(session.getToken())
                .expiresAt(session.getExpiresAt())
                .build());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContextHolder.clear();
    }

    public String resolveToken(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7).trim();
        }
        String xToken = request.getHeader("X-Token");
        if (xToken != null && !xToken.isBlank()) {
            return xToken;
        }
        return request.getParameter("token");
    }

    private void writeUnauthorized(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.fail(status, message)));
    }
}

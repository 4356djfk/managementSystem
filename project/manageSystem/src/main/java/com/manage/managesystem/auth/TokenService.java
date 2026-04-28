package com.manage.managesystem.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class TokenService {
    private static final String TOKEN_KEY_PREFIX = "auth:token:";

    private final AuthProperties authProperties;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public TokenService(AuthProperties authProperties,
                        StringRedisTemplate stringRedisTemplate,
                        ObjectMapper objectMapper) {
        this.authProperties = authProperties;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    public TokenSession createSession(TokenSession session) {
        String token = TokenGenerator.generate();
        session.setToken(token);
        session.setExpiresAt(LocalDateTime.now().plusMinutes(authProperties.getTokenExpireMinutes()));
        try {
            stringRedisTemplate.opsForValue().set(
                    buildKey(token),
                    objectMapper.writeValueAsString(session),
                    Duration.ofMinutes(authProperties.getTokenExpireMinutes())
            );
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("token 会话序列化失败", e);
        }
        return session;
    }

    public TokenSession getValidSession(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        String json = stringRedisTemplate.opsForValue().get(buildKey(token));
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            TokenSession session = objectMapper.readValue(json, TokenSession.class);
            if (session.getExpiresAt() == null || session.getExpiresAt().isBefore(LocalDateTime.now())) {
                stringRedisTemplate.delete(buildKey(token));
                return null;
            }
            return session;
        } catch (JsonProcessingException e) {
            stringRedisTemplate.delete(buildKey(token));
            return null;
        }
    }

    public void invalidate(String token) {
        if (token != null && !token.isBlank()) {
            stringRedisTemplate.delete(buildKey(token));
        }
    }

    public void refreshSession(TokenSession session) {
        if (session == null || session.getToken() == null || session.getToken().isBlank() || session.getExpiresAt() == null) {
            return;
        }
        Duration ttl = Duration.between(LocalDateTime.now(), session.getExpiresAt());
        if (ttl.isZero() || ttl.isNegative()) {
            invalidate(session.getToken());
            return;
        }
        try {
            stringRedisTemplate.opsForValue().set(
                    buildKey(session.getToken()),
                    objectMapper.writeValueAsString(session),
                    ttl
            );
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("token session serialization failed", e);
        }
    }

    private String buildKey(String token) {
        return TOKEN_KEY_PREFIX + token;
    }
}

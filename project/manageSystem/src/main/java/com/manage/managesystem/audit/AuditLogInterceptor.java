package com.manage.managesystem.audit;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.manage.managesystem.auth.AuthUser;
import com.manage.managesystem.auth.UserContextHolder;
import com.manage.managesystem.entity.OperationLogEntity;
import com.manage.managesystem.mapper.OpsMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class AuditLogInterceptor implements HandlerInterceptor {
    private static final int MAX_REQUEST_BODY_LENGTH = 6000;
    private static final List<String> AUDIT_METHODS = List.of("POST", "PUT", "PATCH", "DELETE");
    private static final List<String> SENSITIVE_FIELDS = List.of("password", "passwordHash", "token", "authorization");

    private final OpsMapper opsMapper;
    private final ObjectMapper objectMapper;

    public AuditLogInterceptor(OpsMapper opsMapper, ObjectMapper objectMapper) {
        this.opsMapper = opsMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (!shouldLog(request, handler)) {
            return;
        }

        try {
            OperationLogEntity entity = new OperationLogEntity();
            entity.setId(IdWorker.getId());
            entity.setProjectId(resolveProjectId(request));
            entity.setModuleName(resolveModuleName(request));
            entity.setBizId(resolveBizId(request));
            entity.setAction(resolveAction(request));
            entity.setRequestMethod(request.getMethod());
            entity.setRequestPath(buildRequestPath(request));
            entity.setRequestBody(resolveRequestBody(request));
            entity.setResultCode(resolveResultCode(request, response, ex));
            entity.setIpAddress(resolveIpAddress(request));
            entity.setCreatedAt(LocalDateTime.now());

            applyOperatorInfo(entity, request);
            opsMapper.insertOperationLog(entity);
        } catch (Exception ignored) {
            // 审计日志不能反向影响主流程
        }
    }

    private boolean shouldLog(HttpServletRequest request, Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return false;
        }
        if (!AUDIT_METHODS.contains(request.getMethod())) {
            return false;
        }
        String path = request.getRequestURI();
        return path != null
                && !path.startsWith("/error")
                && !path.startsWith("/swagger-ui")
                && !path.startsWith("/v3/api-docs")
                && !path.startsWith("/webjars");
    }

    private void applyOperatorInfo(OperationLogEntity entity, HttpServletRequest request) {
        Long operatorId = resolveOperatorIdFromRequest(request);
        String operatorName = resolveOperatorNameFromRequest(request);
        if (operatorId != null || StringUtils.hasText(operatorName)) {
            entity.setOperatorId(operatorId);
            entity.setOperatorName(operatorName);
            return;
        }

        AuthUser authUser = UserContextHolder.get();
        if (authUser != null) {
            entity.setOperatorId(authUser.getUserId());
            entity.setOperatorName(StringUtils.hasText(authUser.getRealName()) ? authUser.getRealName() : authUser.getUsername());
            return;
        }

        Object responseData = request.getAttribute(AuditLogContext.RESPONSE_DATA_ATTR);
        operatorId = extractLongFromNode(objectMapper.valueToTree(responseData), "id");
        if (operatorId == null) {
            operatorId = extractNestedLongFromNode(objectMapper.valueToTree(responseData), "user", "id");
        }
        entity.setOperatorId(operatorId);

        operatorName = extractTextFromNode(objectMapper.valueToTree(responseData), "realName", "username");
        if (!StringUtils.hasText(operatorName)) {
            operatorName = extractNestedTextFromNode(objectMapper.valueToTree(responseData), "user", "realName", "username");
        }
        entity.setOperatorName(operatorName);
    }

    private Long resolveProjectId(HttpServletRequest request) {
        Map<String, String> uriVariables = getUriVariables(request);
        Long projectId = parseLong(uriVariables.get("projectId"));
        if (projectId != null) {
            return projectId;
        }

        projectId = parseLong(request.getParameter("projectId"));
        if (projectId != null) {
            return projectId;
        }

        Object responseData = request.getAttribute(AuditLogContext.RESPONSE_DATA_ATTR);
        JsonNode responseNode = objectMapper.valueToTree(responseData);
        projectId = extractLongFromNode(responseNode, "projectId");
        if (projectId != null) {
            return projectId;
        }

        return extractLongFromRequestJson(request, "projectId");
    }

    private Long resolveBizId(HttpServletRequest request) {
        Map<String, String> uriVariables = getUriVariables(request);
        for (String key : List.of("id", "taskId", "memberId")) {
            Long bizId = parseLong(uriVariables.get(key));
            if (bizId != null) {
                return bizId;
            }
        }

        Object responseData = request.getAttribute(AuditLogContext.RESPONSE_DATA_ATTR);
        JsonNode responseNode = objectMapper.valueToTree(responseData);
        Long bizId = extractLongFromNode(responseNode, "id", "bizId", "taskId", "memberId");
        if (bizId != null) {
            return bizId;
        }

        return resolveProjectId(request);
    }

    private String resolveModuleName(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (!StringUtils.hasText(path)) {
            return "SYSTEM";
        }
        if (path.startsWith("/auth")) return "AUTH";
        if (path.startsWith("/users")) return "USER";
        if (path.startsWith("/project-templates")) return "PROJECT_TEMPLATE";
        if (path.startsWith("/notifications")) return "NOTIFICATION";
        if (path.startsWith("/exports")) return "EXPORT";
        if (path.startsWith("/imports")) return "IMPORT";
        if (path.startsWith("/attachments")) return "ATTACHMENT";
        if (path.contains("/editor-preferences")) return "EDITOR_PREFERENCE";
        if (path.contains("/members")) return "PROJECT_MEMBER";
        if (path.contains("/charter")) return "PROJECT_CHARTER";
        if (path.contains("/wbs")) return "WBS";
        if (path.contains("/requirements")) return "REQUIREMENT";
        if (path.contains("/milestones")) return "MILESTONE";
        if (path.contains("/task-dependencies") || path.contains("/tasks") || path.contains("/gantt") || path.contains("/critical-path") || path.contains("/schedule-alerts")) return "TASK";
        if (path.contains("/risks") || path.contains("/risk-matrix")) return "RISK";
        if (path.contains("/cost-") || path.endsWith("/evm")) return "COST";
        if (path.contains("/timesheet")) return "TIMESHEET";
        if (path.contains("/change-requests")) return "CHANGE";
        if (path.contains("/scope-baselines")) return "SCOPE_BASELINE";
        if (path.contains("/acceptance-items")) return "ACCEPTANCE";
        if (path.contains("/archives")) return "ARCHIVE";
        if (path.contains("/lessons-learned")) return "LESSON";
        if (path.contains("/reports") || path.contains("/summary-report")) return "REPORT";
        if (path.contains("/calendar")) return "CALENDAR";
        if (path.contains("/closure-check")) return "CLOSURE";
        if (path.startsWith("/projects")) return "PROJECT";
        return "SYSTEM";
    }

    private String resolveAction(HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getRequestURI();
        if (!StringUtils.hasText(path)) {
            return method;
        }

        if ("POST".equals(method) && path.startsWith("/auth/login")) return "LOGIN";
        if ("POST".equals(method) && path.startsWith("/auth/register")) return "REGISTER";
        if ("POST".equals(method) && path.startsWith("/auth/logout")) return "LOGOUT";
        if ("PUT".equals(method) && path.startsWith("/auth/me")) return "UPDATE_PROFILE";
        if ("POST".equals(method) && path.contains("/from-template")) return "CREATE_FROM_TEMPLATE";
        if ("POST".equals(method) && path.contains("/init-demo")) return "INIT_DEMO";
        if ("POST".equals(method) && path.startsWith("/exports")) return "EXPORT";
        if ("POST".equals(method) && path.startsWith("/imports")) return "IMPORT";
        if ("PATCH".equals(method) && path.contains("/read-all")) return "MARK_ALL_READ";
        if ("PATCH".equals(method) && path.contains("/read")) return "MARK_READ";
        if ("PATCH".equals(method) && path.contains("/status")) return "UPDATE_STATUS";
        if ("PATCH".equals(method) && path.contains("/progress")) return "UPDATE_PROGRESS";
        if ("POST".equals(method) && path.endsWith("/approve")) return "APPROVE";
        if ("POST".equals(method) && path.endsWith("/close")) return "CLOSE";
        if ("POST".equals(method) && path.contains("/summary-report/")) return "GENERATE_SUMMARY";
        if ("POST".equals(method) && path.contains("/generate")) return "GENERATE";
        if ("POST".equals(method) && path.contains("/attachments/upload")) return "UPLOAD";
        if ("POST".equals(method) && path.contains("/comments")) return "CREATE_COMMENT";
        if ("DELETE".equals(method) && path.contains("/comments/")) return "DELETE_COMMENT";
        if ("POST".equals(method) && path.contains("/task-dependencies")) return "CREATE_DEPENDENCY";
        if ("DELETE".equals(method) && path.contains("/task-dependencies/")) return "DELETE_DEPENDENCY";

        return switch (method) {
            case "POST" -> "CREATE";
            case "PUT" -> "UPDATE";
            case "PATCH" -> "PATCH";
            case "DELETE" -> "DELETE";
            default -> method;
        };
    }

    private String buildRequestPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        String query = request.getQueryString();
        return StringUtils.hasText(query) ? path + "?" + query : path;
    }

    private String resolveRequestBody(HttpServletRequest request) {
        if (request instanceof MultipartHttpServletRequest multipartRequest) {
            return truncateQuietly(sanitizeJsonString(buildMultipartSummary(multipartRequest)));
        }
        if (request instanceof ContentCachingRequestWrapper wrapper) {
            byte[] content = wrapper.getContentAsByteArray();
            if (content.length == 0) {
                return null;
            }
            Charset charset = StringUtils.hasText(request.getCharacterEncoding())
                    ? Charset.forName(request.getCharacterEncoding())
                    : StandardCharsets.UTF_8;
            return truncateQuietly(sanitizeJsonString(new String(content, charset)));
        }
        return null;
    }

    private String buildMultipartSummary(MultipartHttpServletRequest request) {
        Map<String, Object> summary = new LinkedHashMap<>();
        request.getParameterMap().forEach((key, value) -> summary.put(key, value == null || value.length == 0 ? null : value.length == 1 ? value[0] : Arrays.asList(value)));
        request.getMultiFileMap().forEach((key, files) -> summary.put(key, files.stream().map(this::describeFile).toList()));
        try {
            return objectMapper.writeValueAsString(summary);
        } catch (JsonProcessingException e) {
            return summary.toString();
        }
    }

    private Map<String, Object> describeFile(MultipartFile file) {
        Map<String, Object> description = new LinkedHashMap<>();
        description.put("fileName", file.getOriginalFilename());
        description.put("size", file.getSize());
        description.put("contentType", file.getContentType());
        return description;
    }

    private String sanitizeJsonString(String content) {
        String normalized = content == null ? null : content.trim();
        if (!StringUtils.hasText(normalized)) {
            return null;
        }
        try {
            JsonNode node = objectMapper.readTree(normalized);
            sanitizeNode(node);
            return objectMapper.writeValueAsString(node);
        } catch (Exception ignored) {
            return normalized;
        }
    }

    private void sanitizeNode(JsonNode node) {
        if (node instanceof ObjectNode objectNode) {
            Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                if (isSensitiveField(field.getKey())) {
                    objectNode.put(field.getKey(), "***");
                } else {
                    sanitizeNode(field.getValue());
                }
            }
            return;
        }
        if (node instanceof ArrayNode arrayNode) {
            for (JsonNode item : arrayNode) {
                sanitizeNode(item);
            }
        }
    }

    private boolean isSensitiveField(String fieldName) {
        if (!StringUtils.hasText(fieldName)) {
            return false;
        }
        return SENSITIVE_FIELDS.stream().anyMatch(item -> item.equalsIgnoreCase(fieldName));
    }

    private String truncateQuietly(String content) {
        if (!StringUtils.hasText(content) || content.length() <= MAX_REQUEST_BODY_LENGTH) {
            return content;
        }
        return content.substring(0, MAX_REQUEST_BODY_LENGTH) + "...(truncated)";
    }

    private Integer resolveResultCode(HttpServletRequest request, HttpServletResponse response, Exception ex) {
        Object attr = request.getAttribute(AuditLogContext.RESULT_CODE_ATTR);
        if (attr instanceof Integer code) {
            return code;
        }
        if (ex instanceof IllegalArgumentException) {
            return 400;
        }
        if (ex != null) {
            return 500;
        }
        if (response.getStatus() >= 400) {
            return response.getStatus();
        }
        return 0;
    }

    private Long resolveOperatorIdFromRequest(HttpServletRequest request) {
        Object operatorId = request.getAttribute(AuditLogContext.OPERATOR_ID_ATTR);
        if (operatorId instanceof Long value) {
            return value;
        }
        if (operatorId instanceof Number value) {
            return value.longValue();
        }
        if (operatorId instanceof String value) {
            return parseLong(value);
        }
        return null;
    }

    private String resolveOperatorNameFromRequest(HttpServletRequest request) {
        Object operatorName = request.getAttribute(AuditLogContext.OPERATOR_NAME_ATTR);
        return operatorName instanceof String value && StringUtils.hasText(value) ? value : null;
    }

    private String resolveIpAddress(HttpServletRequest request) {
        for (String header : List.of("X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP")) {
            String value = request.getHeader(header);
            if (StringUtils.hasText(value) && !"unknown".equalsIgnoreCase(value)) {
                return value.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getUriVariables(HttpServletRequest request) {
        Object attr = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (attr instanceof Map<?, ?> map) {
            return (Map<String, String>) map;
        }
        return Collections.emptyMap();
    }

    private Long extractLongFromRequestJson(HttpServletRequest request, String fieldName) {
        if (!(request instanceof ContentCachingRequestWrapper wrapper)) {
            return null;
        }
        byte[] content = wrapper.getContentAsByteArray();
        if (content.length == 0) {
            return null;
        }
        Charset charset = StringUtils.hasText(request.getCharacterEncoding())
                ? Charset.forName(request.getCharacterEncoding())
                : StandardCharsets.UTF_8;
        try {
            JsonNode node = objectMapper.readTree(new String(content, charset));
            return extractLongFromNode(node, fieldName);
        } catch (Exception ignored) {
            return null;
        }
    }

    private Long extractLongFromNode(JsonNode node, String... fields) {
        if (node == null || node.isNull()) {
            return null;
        }
        for (String field : fields) {
            JsonNode current = node.path(field);
            if (current.isNumber() || current.isTextual()) {
                Long value = parseLong(current.asText());
                if (value != null) {
                    return value;
                }
            }
        }
        return null;
    }

    private Long extractNestedLongFromNode(JsonNode node, String parentField, String childField) {
        if (node == null || node.isNull()) {
            return null;
        }
        return extractLongFromNode(node.path(parentField), childField);
    }

    private String extractTextFromNode(JsonNode node, String... fields) {
        if (node == null || node.isNull()) {
            return null;
        }
        for (String field : fields) {
            JsonNode current = node.path(field);
            if (current.isTextual() && StringUtils.hasText(current.asText())) {
                return current.asText();
            }
        }
        return null;
    }

    private String extractNestedTextFromNode(JsonNode node, String parentField, String... childFields) {
        if (node == null || node.isNull()) {
            return null;
        }
        return extractTextFromNode(node.path(parentField), childFields);
    }

    private Long parseLong(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}

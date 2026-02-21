package com.lexsecura.infrastructure.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class AiSchemaValidator {

    private static final Logger log = LoggerFactory.getLogger(AiSchemaValidator.class);

    private final ObjectMapper objectMapper;
    private final JsonSchemaFactory schemaFactory;
    private final Map<String, JsonSchema> schemaCache = new ConcurrentHashMap<>();

    public AiSchemaValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
    }

    public record ValidationResult(boolean valid, String json, String errors) {}

    public ValidationResult validate(String jsonStr, String schemaPath) {
        try {
            JsonSchema schema = schemaCache.computeIfAbsent(schemaPath, this::loadSchema);
            JsonNode node = objectMapper.readTree(jsonStr);
            Set<ValidationMessage> errors = schema.validate(node);

            if (errors.isEmpty()) {
                return new ValidationResult(true, jsonStr, null);
            }

            String errorMsg = errors.stream()
                    .map(ValidationMessage::getMessage)
                    .collect(Collectors.joining("; "));
            log.warn("JSON validation failed for schema {}: {}", schemaPath, errorMsg);
            return new ValidationResult(false, jsonStr, errorMsg);
        } catch (Exception e) {
            log.error("JSON parsing/validation error", e);
            return new ValidationResult(false, jsonStr, "Invalid JSON: " + e.getMessage());
        }
    }

    private JsonSchema loadSchema(String path) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new IllegalStateException("Schema not found: " + path);
            }
            JsonNode schemaNode = objectMapper.readTree(is);
            return schemaFactory.getSchema(schemaNode);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load schema: " + path, e);
        }
    }
}

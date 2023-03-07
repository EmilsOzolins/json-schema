package org.harrel.jsonschema;

import java.util.List;

public interface AnnotationCollector<T> {
    void onSuccess(ValidationContext ctx, JsonNode schemaNode, JsonNode instanceNode);
    void onFailure(ValidationContext ctx, JsonNode schemaNode, JsonNode instanceNode, String errorMessage);
    T getOutput();
    List<Annotation> getAnnotations();
}

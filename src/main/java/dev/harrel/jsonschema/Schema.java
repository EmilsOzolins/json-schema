package dev.harrel.jsonschema;

import java.net.URI;
import java.util.*;

import static dev.harrel.jsonschema.Evaluator.*;

final class Schema {

    private static final Evaluator TRUE_EVALUATOR = (ctx, node) -> Result.success();
    private static final Evaluator FALSE_EVALUATOR = (ctx, node) -> Result.failure("False schema always fails.");

    private final URI parentUri;
    private final String schemaLocation;
    private final List<EvaluatorWrapper> evaluators;

    Schema(URI parentUri, String schemaLocation, List<EvaluatorWrapper> evaluators) {
        this.parentUri = parentUri;
        this.schemaLocation = Objects.requireNonNull(schemaLocation);
        Objects.requireNonNull(evaluators);
        List<EvaluatorWrapper> unsortedEvaluators = new ArrayList<>(evaluators);
        unsortedEvaluators.sort(Comparator.comparingInt(Evaluator::getOrder));
        this.evaluators = Collections.unmodifiableList(unsortedEvaluators);
    }

    static Evaluator getBooleanEvaluator(boolean val) {
        return val ? TRUE_EVALUATOR : FALSE_EVALUATOR;
    }

    boolean validate(EvaluationContext ctx, JsonNode node) {
        boolean outOfDynamicScope = ctx.isOutOfDynamicScope(parentUri);
        if (outOfDynamicScope) {
            ctx.pushDynamicScope(parentUri);
        }

        int annotationsBefore = ctx.getAnnotations().size();
        boolean valid = true;
        for (EvaluatorWrapper evaluator : evaluators) {
            Result result = evaluator.evaluate(ctx, node);
            Annotation annotation = new Annotation(
                    new AnnotationHeader(evaluator.getKeywordPath(), schemaLocation, node.getJsonPointer()),
                    evaluator.getKeyword(), result.getErrorMessage(), result.isValid());
            ctx.addValidationAnnotation(annotation);
            ctx.addAnnotation(annotation);
            valid = valid && result.isValid();
        }
        if (!valid) {
            ctx.truncateAnnotationsToSize(annotationsBefore);
        }
        if (outOfDynamicScope) {
            ctx.popDynamicContext();
        }
        return valid;
    }
}

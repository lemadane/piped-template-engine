package io.succinct.piped.template.engine.ast;

import io.succinct.piped.template.engine.escapers.AttributeEscaper;
import io.succinct.piped.template.engine.escapers.HtmlEscaper;
import io.succinct.piped.template.engine.escapers.JsonEscaper;
import io.succinct.piped.template.engine.escapers.UrlEscaper;
import io.succinct.piped.template.engine.expression.ExpressionEvaluator;
import io.succinct.piped.template.engine.expression.OutputExpression;
import io.succinct.piped.template.engine.expression.OutputMode;
import io.succinct.piped.template.engine.expression.TemplateContext;
import java.io.IOException;
import java.io.Writer;

public final class ExpressionNode implements ASTNode {
    private static final HtmlEscaper htmlEscaper = new HtmlEscaper();
    private static final AttributeEscaper attributeEscaper = new AttributeEscaper();
    private static final JsonEscaper jsonEscaper = new JsonEscaper();
    private static final UrlEscaper urlEscaper = new UrlEscaper();

    private final OutputExpression outputExpression;
    private final ExpressionEvaluator evaluator;

    public ExpressionNode(OutputExpression outputExpression, ExpressionEvaluator evaluator) {
        this.outputExpression = outputExpression;
        this.evaluator = evaluator;
    }

    public OutputExpression getOutputExpression() {
        return outputExpression;
    }

    @Override
    public void render(TemplateContext context, Writer writer) throws IOException {
        Object value = evaluator.evaluate(outputExpression.expression(), context);
        String formatted = formatValue(outputExpression.mode(), value);
        writer.write(formatted);
    }

    private String formatValue(OutputMode mode, Object value) {
        return switch (mode) {
            case HTML_ESCAPED -> htmlEscaper.escape(value);
            case TRUSTED_HTML -> value == null ? "" : String.valueOf(value);
            case ATTRIBUTE_ESCAPED -> attributeEscaper.escape(value);
            case JSON_ENCODED -> jsonEscaper.escape(value);
            case URL_ENCODED -> urlEscaper.escape(value);
        };
    }
}

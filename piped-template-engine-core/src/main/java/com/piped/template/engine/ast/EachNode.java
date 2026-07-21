package com.piped.template.engine.ast;

import com.piped.template.engine.exceptions.TemplateRenderException;
import com.piped.template.engine.expression.ExpressionEvaluator;
import com.piped.template.engine.expression.TemplateContext;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class EachNode implements ASTNode {
    private final String itemName;
    private final String collectionExpression;
    private final ASTNode bodyBlock;
    private final ASTNode elseBlock;
    private final ExpressionEvaluator evaluator;

    public EachNode(
            String itemName,
            String collectionExpression,
            ASTNode bodyBlock,
            ASTNode elseBlock,
            ExpressionEvaluator evaluator) {
        this.itemName = itemName;
        this.collectionExpression = collectionExpression;
        this.bodyBlock = bodyBlock;
        this.elseBlock = elseBlock;
        this.evaluator = evaluator;
    }

    @Override
    public void render(TemplateContext context, Writer writer) throws IOException {
        Object rawValue = evaluator.evaluate(collectionExpression, context);
        Iterable<?> items = toIterable(rawValue);

        if (items != null && items.iterator().hasNext()) {
            for (Object item : items) {
                TemplateContext subContext = context.subContext(Map.of(itemName, item == null ? "" : item));
                bodyBlock.render(subContext, writer);
            }
        } else if (elseBlock != null) {
            elseBlock.render(context, writer);
        }
    }

    private Iterable<?> toIterable(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Iterable<?> iterable) {
            return iterable;
        }

        if (value instanceof Map<?, ?> map) {
            List<Object> items = new ArrayList<>(map.size());
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                items.add(Map.of("key", entry.getKey(), "value", entry.getValue()));
            }
            return items;
        }

        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            List<Object> items = new ArrayList<>(length);
            for (int index = 0; index < length; index++) {
                items.add(Array.get(value, index));
            }
            return items;
        }

        throw new TemplateRenderException("Value is not iterable: " + value.getClass().getName());
    }
}

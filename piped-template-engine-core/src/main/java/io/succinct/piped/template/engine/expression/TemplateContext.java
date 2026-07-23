package io.succinct.piped.template.engine.expression;

import java.util.HashMap;
import java.util.Map;

public final class TemplateContext {
    private final Map<String, Object> values;
    private final Map<String, Object> localValues = new HashMap<>();

    public TemplateContext(Map<String, Object> values) {
        this.values = values != null ? new HashMap<>(values) : new HashMap<>();
    }

    private TemplateContext(Map<String, Object> parentValues, Map<String, Object> childValues) {
        this.values = new HashMap<>(parentValues);
        if (childValues != null) {
            this.values.putAll(childValues);
        }
    }

    public Object get(String name) {
        if (localValues.containsKey(name)) {
            return localValues.get(name);
        }
        return values.get(name);
    }

    public TemplateContext with(String name, Object value) {
        final var childValues = new HashMap<String, Object>();
        childValues.put(name, value);
        TemplateContext next = new TemplateContext(values, childValues);
        next.localValues.putAll(this.localValues);
        return next;
    }

    public TemplateContext withAll(Map<String, Object> childValues) {
        TemplateContext next = new TemplateContext(values, childValues);
        next.localValues.putAll(this.localValues);
        return next;
    }

    public TemplateContext subContext(Map<String, Object> childValues) {
        return withAll(childValues);
    }

    public void pushLocal(String name, Object value) {
        localValues.put(name, value);
    }
}
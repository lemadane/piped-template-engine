package io.succinct.piped.template.engine.expression;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class TemplateContext {
   private final Map<String, Object> values;

   public TemplateContext(Map<String, Object> values) {
      if (values == null) {
         this.values = Collections.emptyMap();
         return;
      }

      this.values = Collections.unmodifiableMap(new HashMap<>(values));
   }

   private TemplateContext(Map<String, Object> parentValues, Map<String, Object> childValues) {
      final var mergedValues = new HashMap<>(parentValues);
      mergedValues.putAll(childValues);

      this.values = Collections.unmodifiableMap(mergedValues);
   }

   public Object get(String name) {
      return values.get(name);
   }

   public TemplateContext with(String name, Object value) {
      final var childValues = new HashMap<String, Object>();
      childValues.put(name, value);

      return new TemplateContext(values, childValues);
   }

   public TemplateContext withAll(Map<String, Object> childValues) {
      return new TemplateContext(values, childValues);
   }
}
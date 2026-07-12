package com.piped.template.engine.parsers;

import com.piped.template.engine.exceptions.TemplateSyntaxException;
import com.piped.template.engine.expression.OutputExpression;
import com.piped.template.engine.expression.OutputMode;

public final class OutputExpressionParser {
   public OutputExpression parse(String source) {
      if (source == null || source.isBlank()) {
         throw new TemplateSyntaxException("Output expression must not be empty.");
      }
      final var trimmedSource = source.trim();
      if (trimmedSource.startsWith("html ")) {
         return new OutputExpression(
               OutputMode.TRUSTED_HTML,
               trimmedSource.substring("html ".length()).trim());
      }
      if (trimmedSource.startsWith("attr ")) {
         return new OutputExpression(
               OutputMode.ATTRIBUTE_ESCAPED,
               trimmedSource.substring("attr ".length()).trim());
      }
      if (trimmedSource.startsWith("url ")) {
         return new OutputExpression(
               OutputMode.URL_ENCODED,
               trimmedSource.substring("url ".length()).trim());
      }

      if (trimmedSource.startsWith("json ")) {
         return new OutputExpression(
               OutputMode.JSON_ENCODED,
               trimmedSource.substring("json ".length()).trim());
      }
      return new OutputExpression(
            OutputMode.HTML_ESCAPED,
            trimmedSource);
   }
}
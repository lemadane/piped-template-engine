package io.succinct.piped.template.engine.statements;

public record IncludeStatement(
      String templateName,
      String contextExpression,
      boolean hasContextExpression) {
}
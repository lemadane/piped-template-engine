package io.succinct.piped.template.engine.compiler;

public record Token(
    TokenType type,
    String value,
    int position
) {}

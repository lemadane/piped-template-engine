package com.piped.template.engine.compiler;

public record Token(
    TokenType type,
    String value,
    int position
) {}

package com.piped.template.engine.compiler;

public enum TokenType {
    TEXT,
    EXPRESSION,
    IF,
    ELSE_IF,
    ELSE,
    END_IF,
    EACH,
    END_EACH,
    SWITCH,
    CASE,
    DEFAULT,
    FALLTHROUGH,
    END_SWITCH,
    INCLUDE,
    LAYOUT,
    SECTION,
    END_SECTION,
    YIELD,
    COMPONENT,
    END_COMPONENT,
    SLOT,
    END_SLOT,
    COMMENT
}

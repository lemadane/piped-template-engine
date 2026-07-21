package com.piped.template.engine.compiler;

import com.piped.template.engine.exceptions.TemplateSyntaxException;
import java.util.ArrayList;
import java.util.List;

public final class Lexer {

    public List<Token> tokenize(String template) {
        List<Token> tokens = new ArrayList<>();
        if (template == null || template.isEmpty()) {
            return tokens;
        }

        int length = template.length();
        int cursor = 0;

        while (cursor < length) {
            int pipeIndex = template.indexOf('|', cursor);

            if (pipeIndex == -1) {
                tokens.add(new Token(TokenType.TEXT, template.substring(cursor), cursor));
                break;
            }

            if (pipeIndex > cursor) {
                tokens.add(new Token(TokenType.TEXT, template.substring(cursor, pipeIndex), cursor));
            }

            // Check for comment |-- ... --|
            if (template.startsWith("|--", pipeIndex)) {
                int commentEnd = template.indexOf("--|", pipeIndex + 3);
                if (commentEnd == -1) {
                    throw new TemplateSyntaxException("Unclosed comment starting at index " + pipeIndex);
                }
                tokens.add(new Token(TokenType.COMMENT, template.substring(pipeIndex + 3, commentEnd), pipeIndex));
                cursor = commentEnd + 3;
                continue;
            }

            // Standard expression or directive pipe
            int closingPipe = template.indexOf('|', pipeIndex + 1);
            if (closingPipe == -1) {
                throw new TemplateSyntaxException("Missing closing pipe for expression starting at index " + pipeIndex);
            }

            String content = template.substring(pipeIndex + 1, closingPipe).trim();
            TokenType type = classifyToken(content);

            tokens.add(new Token(type, content, pipeIndex));
            cursor = closingPipe + 1;
        }

        return tokens;
    }

    private TokenType classifyToken(String content) {
        if (content.startsWith("if ")) {
            return TokenType.IF;
        } else if (content.startsWith("else if ") || content.startsWith("else-if ")) {
            return TokenType.ELSE_IF;
        } else if ("else".equals(content)) {
            return TokenType.ELSE;
        } else if ("/if".equals(content)) {
            return TokenType.END_IF;
        } else if (content.startsWith("each ")) {
            return TokenType.EACH;
        } else if ("/each".equals(content)) {
            return TokenType.END_EACH;
        } else if (content.startsWith("switch ")) {
            return TokenType.SWITCH;
        } else if (content.startsWith("case ")) {
            return TokenType.CASE;
        } else if ("default".equals(content)) {
            return TokenType.DEFAULT;
        } else if ("fallthrough".equals(content)) {
            return TokenType.FALLTHROUGH;
        } else if ("/switch".equals(content)) {
            return TokenType.END_SWITCH;
        } else if (content.startsWith("include ")) {
            return TokenType.INCLUDE;
        } else if (content.startsWith("layout ")) {
            return TokenType.LAYOUT;
        } else if (content.startsWith("section ")) {
            return TokenType.SECTION;
        } else if ("/section".equals(content)) {
            return TokenType.END_SECTION;
        } else if (content.startsWith("yield ")) {
            return TokenType.YIELD;
        } else if (content.startsWith("component ")) {
            return TokenType.COMPONENT;
        } else if ("/component".equals(content)) {
            return TokenType.END_COMPONENT;
        } else if (content.startsWith("slot ")) {
            return TokenType.SLOT;
        } else if ("/slot".equals(content)) {
            return TokenType.END_SLOT;
        } else if (content.startsWith("model ")) {
            return TokenType.MODEL;
        } else if (content.startsWith("field ")) {
            return TokenType.FIELD;
        } else if (content.startsWith("display ")) {
            return TokenType.DISPLAY;
        } else if (content.startsWith("editor ")) {
            return TokenType.EDITOR;
        } else if (content.startsWith("macro ")) {
            return TokenType.MACRO;
        } else if ("/macro".equals(content)) {
            return TokenType.END_MACRO;
        } else if (content.startsWith("call ")) {
            return TokenType.CALL;
        } else if ("separator".equals(content)) {
            return TokenType.SEPARATOR;
        } else if ("/separator".equals(content)) {
            return TokenType.END_SEPARATOR;
        } else if (content.startsWith("fragment ")) {
            return TokenType.FRAGMENT;
        } else if ("/fragment".equals(content)) {
            return TokenType.END_FRAGMENT;
        }
        return TokenType.EXPRESSION;
    }
}

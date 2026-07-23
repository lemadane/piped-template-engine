package io.succinct.piped.template.engine.compiler;

import io.succinct.piped.template.engine.ast.*;
import io.succinct.piped.template.engine.exceptions.TemplateSyntaxException;
import io.succinct.piped.template.engine.expression.ExpressionEvaluator;
import io.succinct.piped.template.engine.parsers.OutputExpressionParser;
import java.util.ArrayList;
import java.util.List;

public final class Parser {
    private final OutputExpressionParser outputExpressionParser = new OutputExpressionParser();
    private final ExpressionEvaluator evaluator = new ExpressionEvaluator();
    private final ThreadLocal<java.util.Map<String, Object>> threadLocalMetadata = ThreadLocal.withInitial(java.util.HashMap::new);

    public CompiledTemplate parse(List<Token> tokens) {
        Cursor cursor = new Cursor(tokens);
        threadLocalMetadata.get().clear();
        BlockNode root = parseBlock(cursor, null);
        java.util.Map<String, Object> metadata = new java.util.HashMap<>(threadLocalMetadata.get());
        threadLocalMetadata.get().clear();
        return new CompiledTemplate(root, metadata);
    }

    private BlockNode parseBlock(Cursor cursor, TokenType stopToken) {
        List<ASTNode> nodes = new ArrayList<>();

        while (cursor.hasNext()) {
            Token token = cursor.peek();

            if (stopToken != null && token.type() == stopToken) {
                break;
            }

            if (token.type() == TokenType.ELSE || token.type() == TokenType.ELSE_IF) {
                break;
            }

            cursor.next();

            switch (token.type()) {
                case TEXT -> nodes.add(new TextNode(token.value()));
                case COMMENT -> { /* Ignore comments */ }
                case EXPRESSION -> {
                    var outputExpr = outputExpressionParser.parse(token.value());
                    nodes.add(new ExpressionNode(outputExpr, evaluator));
                }
                case IF -> nodes.add(parseIf(token, cursor));
                case EACH -> nodes.add(parseEach(token, cursor));
                case MODEL -> nodes.add(new ModelNode(token.value().substring("model ".length()).trim()));
                case FIELD -> nodes.add(new FieldNode(token.value().substring("field ".length()).trim(), evaluator));
                case DISPLAY -> nodes.add(new DisplayNode(token.value().substring("display ".length()).trim(), evaluator));
                case EDITOR -> nodes.add(new EditorNode(token.value().substring("editor ".length()).trim(), evaluator));
                case MACRO -> nodes.add(parseMacro(token, cursor));
                case CALL -> nodes.add(parseCallMacro(token));
                case SEPARATOR -> {
                    if (eachDepth == 0) {
                        throw new TemplateSyntaxException("|separator| is only allowed directly inside an |each| loop.");
                    }
                    ASTNode sepBody = parseBlock(cursor, TokenType.END_SEPARATOR);
                    if (cursor.hasNext() && cursor.peek().type() == TokenType.END_SEPARATOR) {
                        cursor.next();
                    }
                    nodes.add(new io.succinct.piped.template.engine.ast.SeparatorNode(sepBody));
                }
                case FRAGMENT -> nodes.add(parseFragment(token, cursor));
                case MINIFY -> nodes.add(parseMinify(token, cursor));
                case PAGE -> parsePageMetadata(token);
                case ATTEMPT -> nodes.add(parseAttempt(token, cursor));
                default -> {
                    var outputExpr = outputExpressionParser.parse(token.value());
                    nodes.add(new ExpressionNode(outputExpr, evaluator));
                }
            }
        }

        return new BlockNode(nodes);
    }

    private IfNode parseIf(Token ifToken, Cursor cursor) {
        String condition = ifToken.value().substring("if ".length()).trim();
        ASTNode thenBlock = parseBlock(cursor, TokenType.END_IF);

        List<IfNode.ElseIfBranch> elseIfBranches = new ArrayList<>();
        ASTNode elseBlock = null;

        while (cursor.hasNext() && cursor.peek().type() != TokenType.END_IF) {
            Token current = cursor.peek();
            if (current.type() == TokenType.ELSE_IF) {
                cursor.next();
                String elseIfCondition = current.value().startsWith("else-if ")
                        ? current.value().substring("else-if ".length()).trim()
                        : current.value().substring("else if ".length()).trim();
                ASTNode elseIfBody = parseBlock(cursor, TokenType.END_IF);
                elseIfBranches.add(new IfNode.ElseIfBranch(elseIfCondition, elseIfBody));
            } else if (current.type() == TokenType.ELSE) {
                cursor.next();
                elseBlock = parseBlock(cursor, TokenType.END_IF);
                break;
            } else {
                break;
            }
        }

        if (cursor.hasNext() && cursor.peek().type() == TokenType.END_IF) {
            cursor.next();
        }

        return new IfNode(condition, thenBlock, elseIfBranches, elseBlock, evaluator);
    }

    private int eachDepth = 0;

    private EachNode parseEach(Token eachToken, Cursor cursor) {
        eachDepth++;
        try {
            String statement = eachToken.value().substring("each ".length()).trim();
            int inIndex = statement.indexOf(" in ");
            if (inIndex == -1) {
                throw new TemplateSyntaxException("Invalid each statement format. Expected '|each item in items|'");
            }

            String itemName = statement.substring(0, inIndex).trim();
            String collectionExpr = statement.substring(inIndex + 4).trim();

            ASTNode bodyBlock = parseBlock(cursor, TokenType.END_EACH);
            ASTNode elseBlock = null;

            if (cursor.hasNext() && cursor.peek().type() == TokenType.ELSE) {
                cursor.next();
                elseBlock = parseBlock(cursor, TokenType.END_EACH);
            }

            if (cursor.hasNext() && cursor.peek().type() == TokenType.END_EACH) {
                cursor.next();
            }

            ASTNode separatorNode = null;
            if (bodyBlock instanceof BlockNode blockNode) {
                List<ASTNode> bodyChildren = new ArrayList<>();
                for (ASTNode child : blockNode.getChildren()) {
                    if (child instanceof io.succinct.piped.template.engine.ast.SeparatorNode sep) {
                        separatorNode = sep;
                    } else {
                        bodyChildren.add(child);
                    }
                }
                bodyBlock = new BlockNode(bodyChildren);
            }

            return new EachNode(itemName, collectionExpr, bodyBlock, elseBlock, separatorNode, evaluator);
        } finally {
            eachDepth--;
        }
    }

    private io.succinct.piped.template.engine.ast.MacroNode parseMacro(Token macroToken, Cursor cursor) {
        String val = macroToken.value().substring("macro ".length()).trim();
        int openParen = val.indexOf('(');
        int closeParen = val.indexOf(')');

        String name;
        List<String> params = new ArrayList<>();
        if (openParen != -1 && closeParen > openParen) {
            name = val.substring(0, openParen).trim();
            String argsStr = val.substring(openParen + 1, closeParen).trim();
            if (!argsStr.isEmpty()) {
                for (String p : argsStr.split(",")) {
                    params.add(p.trim());
                }
            }
        } else {
            name = val;
        }

        ASTNode body = parseBlock(cursor, TokenType.END_MACRO);
        if (cursor.hasNext() && cursor.peek().type() == TokenType.END_MACRO) {
            cursor.next();
        }

        return new io.succinct.piped.template.engine.ast.MacroNode(name, params, body);
    }

    private io.succinct.piped.template.engine.ast.CallMacroNode parseCallMacro(Token callToken) {
        String val = callToken.value().substring("call ".length()).trim();
        int openParen = val.indexOf('(');
        int closeParen = val.lastIndexOf(')');

        String name;
        List<String> args = new ArrayList<>();
        if (openParen != -1 && closeParen > openParen) {
            name = val.substring(0, openParen).trim();
            String argsStr = val.substring(openParen + 1, closeParen).trim();
            if (!argsStr.isEmpty()) {
                for (String arg : argsStr.split(",")) {
                    args.add(arg.trim());
                }
            }
        } else {
            name = val;
        }

        return new io.succinct.piped.template.engine.ast.CallMacroNode(name, args, evaluator);
    }

    private io.succinct.piped.template.engine.ast.FragmentNode parseFragment(Token fragmentToken, Cursor cursor) {
        String name = fragmentToken.value().substring("fragment ".length()).trim();
        ASTNode body = parseBlock(cursor, TokenType.END_FRAGMENT);
        if (cursor.hasNext() && cursor.peek().type() == TokenType.END_FRAGMENT) {
            cursor.next();
        }
        return new io.succinct.piped.template.engine.ast.FragmentNode(name, body);
    }

    private io.succinct.piped.template.engine.ast.MinifyNode parseMinify(Token minifyToken, Cursor cursor) {
        ASTNode body = parseBlock(cursor, TokenType.END_MINIFY);
        if (cursor.hasNext() && cursor.peek().type() == TokenType.END_MINIFY) {
            cursor.next();
        }
        return new io.succinct.piped.template.engine.ast.MinifyNode(body);
    }

    private void parsePageMetadata(Token token) {
        String val = token.value().substring("page ".length()).trim();
        int eqIndex = val.indexOf('=');
        if (eqIndex != -1) {
            String key = val.substring(0, eqIndex).trim();
            String valueStr = val.substring(eqIndex + 1).trim();
            Object value = parseMetadataValue(valueStr);
            threadLocalMetadata.get().put(key, value);
        }
    }

    private Object parseMetadataValue(String str) {
        if (str.startsWith("\"") && str.endsWith("\"")) {
            return str.substring(1, str.length() - 1);
        }
        if (str.startsWith("'") && str.endsWith("'")) {
            return str.substring(1, str.length() - 1);
        }
        if ("true".equalsIgnoreCase(str)) {
            return Boolean.TRUE;
        }
        if ("false".equalsIgnoreCase(str)) {
            return Boolean.FALSE;
        }
        if (str.startsWith("[") && str.endsWith("]")) {
            String inner = str.substring(1, str.length() - 1).trim();
            if (inner.isEmpty()) {
                return List.of();
            }
            List<String> items = new ArrayList<>();
            for (String item : inner.split(",")) {
                String trimmed = item.trim();
                if ((trimmed.startsWith("\"") && trimmed.endsWith("\"")) || (trimmed.startsWith("'") && trimmed.endsWith("'"))) {
                    trimmed = trimmed.substring(1, trimmed.length() - 1);
                }
                items.add(trimmed);
            }
            return items;
        }
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            // Ignore
        }
        return str;
    }

    private io.succinct.piped.template.engine.ast.AttemptNode parseAttempt(Token attemptToken, Cursor cursor) {
        ASTNode body = parseBlock(cursor, TokenType.RECOVER);

        if (!cursor.hasNext() || cursor.peek().type() != TokenType.RECOVER) {
            throw new TemplateSyntaxException("Missing matching |recover| block inside |attempt|.");
        }

        Token recoverToken = cursor.next();
        String errorVarName = null;
        String val = recoverToken.value().substring("recover".length()).trim();
        if (val.startsWith("as ")) {
            errorVarName = val.substring("as ".length()).trim();
        }

        ASTNode recoverBlock = parseBlock(cursor, TokenType.END_ATTEMPT);
        if (cursor.hasNext() && cursor.peek().type() == TokenType.END_ATTEMPT) {
            cursor.next();
        }

        return new io.succinct.piped.template.engine.ast.AttemptNode(body, recoverBlock, errorVarName);
    }

    private static class Cursor {
        private final List<Token> tokens;
        private int index = 0;

        Cursor(List<Token> tokens) {
            this.tokens = tokens;
        }

        boolean hasNext() {
            return index < tokens.size();
        }

        Token peek() {
            return tokens.get(index);
        }

        Token next() {
            return tokens.get(index++);
        }
    }
}

package com.piped.template.engine.codegen;

import com.piped.template.engine.ast.ASTNode;
import com.piped.template.engine.ast.BlockNode;
import com.piped.template.engine.ast.ExpressionNode;
import com.piped.template.engine.ast.TextNode;
import java.util.concurrent.atomic.AtomicInteger;

public final class JavaCodeGenerator {
    private static final AtomicInteger COUNTER = new AtomicInteger();

    public String generateClassSource(ASTNode rootNode, String className) {
        StringBuilder sb = new StringBuilder();
        sb.append("package com.piped.template.engine.codegen.generated;\n\n");
        sb.append("import com.piped.template.engine.TemplateEngine;\n");
        sb.append("import com.piped.template.engine.codegen.CompiledTemplateExecutable;\n");
        sb.append("import com.piped.template.engine.expression.TemplateContext;\n");
        sb.append("import java.io.IOException;\n");
        sb.append("import java.io.Writer;\n");
        sb.append("import java.util.Map;\n\n");
        sb.append("public final class ").append(className).append(" implements CompiledTemplateExecutable {\n");
        sb.append("    @Override\n");
        sb.append("    public void render(TemplateContext context, Writer writer, TemplateEngine engine) throws IOException {\n");

        generateNodeSource(rootNode, sb, "        ");

        sb.append("    }\n");
        sb.append("}\n");

        return sb.toString();
    }

    private void generateNodeSource(ASTNode node, StringBuilder sb, String indent) {
        if (node instanceof TextNode textNode) {
            sb.append(indent).append("writer.write(").append(escapeStringLiteral(textNode.getText())).append(");\n");
        } else if (node instanceof ExpressionNode exprNode) {
            sb.append(indent).append("writer.write(engine.evaluateExpression(")
                    .append(escapeStringLiteral(exprNode.getOutputExpression().expression())).append(", ")
                    .append(escapeStringLiteral(exprNode.getOutputExpression().mode().name())).append(", context));\n");
        } else if (node instanceof BlockNode blockNode) {
            for (ASTNode child : blockNode.getChildren()) {
                generateNodeSource(child, sb, indent);
            }
        } else if (node instanceof com.piped.template.engine.ast.FieldNode fieldNode) {
            sb.append(indent).append("new com.piped.template.engine.ast.FieldNode(")
                    .append(escapeStringLiteral(fieldNode.getPropertyPath())).append(", new com.piped.template.engine.expression.ExpressionEvaluator()).render(context, writer);\n");
        } else if (node instanceof com.piped.template.engine.ast.DisplayNode displayNode) {
            sb.append(indent).append("new com.piped.template.engine.ast.DisplayNode(")
                    .append(escapeStringLiteral(displayNode.getPropertyPath())).append(", new com.piped.template.engine.expression.ExpressionEvaluator()).render(context, writer);\n");
        } else if (node instanceof com.piped.template.engine.ast.EditorNode editorNode) {
            sb.append(indent).append("new com.piped.template.engine.ast.EditorNode(")
                    .append(escapeStringLiteral(editorNode.getPropertyPath())).append(", new com.piped.template.engine.expression.ExpressionEvaluator()).render(context, writer);\n");
        } else if (node instanceof com.piped.template.engine.ast.MacroNode macroNode) {
            sb.append(indent).append("new com.piped.template.engine.ast.MacroNode(")
                    .append(escapeStringLiteral(macroNode.getName())).append(", ")
                    .append("java.util.List.of(");
            for (int i = 0; i < macroNode.getParameters().size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(escapeStringLiteral(macroNode.getParameters().get(i)));
            }
            sb.append("), null).render(context, writer);\n");
        } else if (node instanceof com.piped.template.engine.ast.CallMacroNode callNode) {
            sb.append(indent).append("new com.piped.template.engine.ast.CallMacroNode(")
                    .append(escapeStringLiteral(callNode.getMacroName())).append(", ")
                    .append("java.util.List.of(");
            for (int i = 0; i < callNode.getArgumentExpressions().size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(escapeStringLiteral(callNode.getArgumentExpressions().get(i)));
            }
            sb.append("), new com.piped.template.engine.expression.ExpressionEvaluator()).render(context, writer);\n");
        } else if (node instanceof com.piped.template.engine.ast.SeparatorNode sepNode) {
            generateNodeSource(sepNode.getBody(), sb, indent);
        } else if (node instanceof com.piped.template.engine.ast.FragmentNode fragNode) {
            generateNodeSource(fragNode.getBody(), sb, indent);
        }
    }

    public static String generateUniqueClassName() {
        return "Template_Gen_" + System.currentTimeMillis() + "_" + COUNTER.incrementAndGet();
    }

    private String escapeStringLiteral(String value) {
        if (value == null) {
            return "null";
        }
        return "\"" + value.replace("\\", "\\\\")
                           .replace("\"", "\\\"")
                           .replace("\n", "\\n")
                           .replace("\r", "\\r")
                           .replace("\t", "\\t") + "\"";
    }
}

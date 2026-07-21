package com.piped.template.engine.compiler;

import com.piped.template.engine.ast.ASTNode;
import com.piped.template.engine.expression.TemplateContext;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public final class CompiledTemplate {
    private final ASTNode rootNode;

    public CompiledTemplate(ASTNode rootNode) {
        this.rootNode = rootNode;
    }

    public ASTNode getRootNode() {
        return rootNode;
    }

    public void render(TemplateContext context, Writer writer) throws IOException {
        rootNode.render(context, writer);
    }

    public String renderToString(TemplateContext context) {
        StringWriter writer = new StringWriter();
        try {
            render(context, writer);
            return writer.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

package com.piped.template.engine.ast;

import com.piped.template.engine.expression.TemplateContext;
import java.io.IOException;
import java.io.Writer;

@FunctionalInterface
public interface ASTNode {
    void render(TemplateContext context, Writer writer) throws IOException;
}

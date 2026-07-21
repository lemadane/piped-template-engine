package io.succinct.piped.template.engine.codegen;

import io.succinct.piped.template.engine.TemplateEngine;
import io.succinct.piped.template.engine.expression.TemplateContext;
import java.io.IOException;
import java.io.Writer;

@FunctionalInterface
public interface CompiledTemplateExecutable {
    void render(TemplateContext context, Writer writer, TemplateEngine engine) throws IOException;
}

package io.succinct.piped.template.engine.ast;

import io.succinct.piped.template.engine.expression.ExpressionEvaluator;
import io.succinct.piped.template.engine.expression.TemplateContext;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

public final class IfNode implements ASTNode {
    public record ElseIfBranch(String condition, ASTNode body) {}

    private final String ifCondition;
    private final ASTNode thenBlock;
    private final List<ElseIfBranch> elseIfBranches;
    private final ASTNode elseBlock;
    private final ExpressionEvaluator evaluator;

    public IfNode(
            String ifCondition,
            ASTNode thenBlock,
            List<ElseIfBranch> elseIfBranches,
            ASTNode elseBlock,
            ExpressionEvaluator evaluator) {
        this.ifCondition = ifCondition;
        this.thenBlock = thenBlock;
        this.elseIfBranches = List.copyOf(elseIfBranches);
        this.elseBlock = elseBlock;
        this.evaluator = evaluator;
    }

    @Override
    public void render(TemplateContext context, Writer writer) throws IOException {
        if (evaluator.evaluateBoolean(ifCondition, context)) {
            thenBlock.render(context, writer);
            return;
        }

        for (int i = 0; i < elseIfBranches.size(); i++) {
            ElseIfBranch branch = elseIfBranches.get(i);
            if (evaluator.evaluateBoolean(branch.condition(), context)) {
                branch.body().render(context, writer);
                return;
            }
        }

        if (elseBlock != null) {
            elseBlock.render(context, writer);
        }
    }
}

package goblinbob.mobends.core.expression.ast;

import goblinbob.mobends.core.expression.ExpressionContext;

public interface ExpressionNode {
    double evaluate(ExpressionContext context);

    default boolean isConstant() {
        return false;
    }

    default ExpressionNode optimize() {
        return this;
    }
}

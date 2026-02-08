package goblinbob.mobends.core.expression.ast;

import goblinbob.mobends.core.expression.ExpressionContext;

/**
 * AST node representing a numeric literal (e.g., 3.14, -5, 0).
 */
public class LiteralNode implements ExpressionNode {
    private final double value;

    public LiteralNode(double value) {
        this.value = value;
    }

    @Override
    public double evaluate(ExpressionContext context) {
        return value;
    }

    @Override
    public boolean isConstant() {
        return true;
    }

    public double getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}

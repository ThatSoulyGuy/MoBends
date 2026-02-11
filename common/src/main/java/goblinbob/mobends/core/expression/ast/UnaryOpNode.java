package goblinbob.mobends.core.expression.ast;

import goblinbob.mobends.core.expression.ExpressionContext;

/**
 * AST node representing a unary operation (e.g., -x, !x).
 */
public class UnaryOpNode implements ExpressionNode {
    private final ExpressionNode operand;
    private final Operator operator;

    public enum Operator {
        NEGATE("-"),
        NOT("!");

        private final String symbol;

        Operator(String symbol) {
            this.symbol = symbol;
        }

        public String getSymbol() {
            return symbol;
        }
    }

    public UnaryOpNode(Operator operator, ExpressionNode operand) {
        this.operator = operator;
        this.operand = operand;
    }

    @Override
    public double evaluate(ExpressionContext context) {
        double value = operand.evaluate(context);
        return switch (operator) {
            case NEGATE -> -value;
            case NOT -> value == 0.0 ? 1.0 : 0.0;
        };
    }

    @Override
    public boolean isConstant() {
        return operand.isConstant();
    }

    @Override
    public ExpressionNode optimize() {
        ExpressionNode optimized = operand.optimize();
        if (optimized.isConstant()) {
            return new LiteralNode(new UnaryOpNode(operator, optimized).evaluate(null));
        }
        if (optimized != operand) {
            return new UnaryOpNode(operator, optimized);
        }
        return this;
    }

    public ExpressionNode getOperand() {
        return operand;
    }

    public Operator getOperator() {
        return operator;
    }

    @Override
    public String toString() {
        return operator.getSymbol() + operand;
    }
}

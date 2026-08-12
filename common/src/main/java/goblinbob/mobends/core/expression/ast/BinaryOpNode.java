package goblinbob.mobends.core.expression.ast;

import goblinbob.mobends.core.expression.ExpressionContext;

public class BinaryOpNode implements ExpressionNode {
    private final ExpressionNode left;
    private final ExpressionNode right;
    private final Operator operator;

    public enum Operator {
        ADD("+"),
        SUBTRACT("-"),
        MULTIPLY("*"),
        DIVIDE("/"),
        MODULO("%"),
        POWER("^"),
        LESS_THAN("<"),
        GREATER_THAN(">"),
        LESS_EQUAL("<="),
        GREATER_EQUAL(">="),
        EQUAL("=="),
        NOT_EQUAL("!="),
        AND("&&"),
        OR("||");

        private final String symbol;

        Operator(String symbol) {
            this.symbol = symbol;
        }

        public String getSymbol() {
            return symbol;
        }
    }

    public BinaryOpNode(ExpressionNode left, Operator operator, ExpressionNode right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public double evaluate(ExpressionContext context) {
        if (operator == Operator.AND) {
            double leftVal = left.evaluate(context);
            if (leftVal == 0.0) return 0.0;
            return right.evaluate(context) != 0.0 ? 1.0 : 0.0;
        }
        if (operator == Operator.OR) {
            double leftVal = left.evaluate(context);
            if (leftVal != 0.0) return 1.0;
            return right.evaluate(context) != 0.0 ? 1.0 : 0.0;
        }

        double leftVal = left.evaluate(context);
        double rightVal = right.evaluate(context);

        return switch (operator) {
            case ADD -> leftVal + rightVal;
            case SUBTRACT -> leftVal - rightVal;
            case MULTIPLY -> leftVal * rightVal;
            case DIVIDE -> rightVal != 0.0 ? leftVal / rightVal : 0.0;
            case MODULO -> rightVal != 0.0 ? leftVal % rightVal : 0.0;
            case POWER -> Math.pow(leftVal, rightVal);
            case LESS_THAN -> leftVal < rightVal ? 1.0 : 0.0;
            case GREATER_THAN -> leftVal > rightVal ? 1.0 : 0.0;
            case LESS_EQUAL -> leftVal <= rightVal ? 1.0 : 0.0;
            case GREATER_EQUAL -> leftVal >= rightVal ? 1.0 : 0.0;
            case EQUAL -> leftVal == rightVal ? 1.0 : 0.0;
            case NOT_EQUAL -> leftVal != rightVal ? 1.0 : 0.0;
            case AND, OR -> throw new IllegalStateException("Should be handled by short-circuit");
        };
    }

    @Override
    public boolean isConstant() {
        return left.isConstant() && right.isConstant();
    }

    @Override
    public ExpressionNode optimize() {
        ExpressionNode optimizedLeft = left.optimize();
        ExpressionNode optimizedRight = right.optimize();

        if (optimizedLeft.isConstant() && optimizedRight.isConstant()) {
            return new LiteralNode(new BinaryOpNode(optimizedLeft, operator, optimizedRight).evaluate(null));
        }

        if (optimizedLeft != left || optimizedRight != right) {
            return new BinaryOpNode(optimizedLeft, operator, optimizedRight);
        }
        return this;
    }

    public ExpressionNode getLeft() {
        return left;
    }

    public ExpressionNode getRight() {
        return right;
    }

    public Operator getOperator() {
        return operator;
    }

    @Override
    public String toString() {
        return "(" + left + " " + operator.getSymbol() + " " + right + ")";
    }
}

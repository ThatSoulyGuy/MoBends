package goblinbob.mobends.core.expression.ast;

import goblinbob.mobends.core.expression.ExpressionContext;

/**
 * AST node representing a ternary conditional expression (e.g., a ? b : c).
 */
public class TernaryNode implements ExpressionNode {
    private final ExpressionNode condition;
    private final ExpressionNode thenBranch;
    private final ExpressionNode elseBranch;

    public TernaryNode(ExpressionNode condition, ExpressionNode thenBranch, ExpressionNode elseBranch) {
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    @Override
    public double evaluate(ExpressionContext context) {
        // Short-circuit: only evaluate the branch we need
        if (condition.evaluate(context) != 0.0) {
            return thenBranch.evaluate(context);
        } else {
            return elseBranch.evaluate(context);
        }
    }

    @Override
    public boolean isConstant() {
        return condition.isConstant() && thenBranch.isConstant() && elseBranch.isConstant();
    }

    @Override
    public ExpressionNode optimize() {
        ExpressionNode optimizedCondition = condition.optimize();
        ExpressionNode optimizedThen = thenBranch.optimize();
        ExpressionNode optimizedElse = elseBranch.optimize();

        // If condition is constant, we can eliminate the branch entirely
        if (optimizedCondition.isConstant()) {
            if (optimizedCondition.evaluate(null) != 0.0) {
                return optimizedThen;
            } else {
                return optimizedElse;
            }
        }

        if (optimizedCondition != condition || optimizedThen != thenBranch || optimizedElse != elseBranch) {
            return new TernaryNode(optimizedCondition, optimizedThen, optimizedElse);
        }
        return this;
    }

    public ExpressionNode getCondition() {
        return condition;
    }

    public ExpressionNode getThenBranch() {
        return thenBranch;
    }

    public ExpressionNode getElseBranch() {
        return elseBranch;
    }

    @Override
    public String toString() {
        return "(" + condition + " ? " + thenBranch + " : " + elseBranch + ")";
    }
}

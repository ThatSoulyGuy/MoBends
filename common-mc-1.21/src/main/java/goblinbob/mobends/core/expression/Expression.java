package goblinbob.mobends.core.expression;

import goblinbob.mobends.core.expression.ast.ExpressionNode;

/**
 * A compiled expression that can be evaluated with a given context.
 * Expressions are parsed once and cached for efficient repeated evaluation.
 */
public class Expression {
    private final String source;
    private final ExpressionNode root;

    Expression(String source, ExpressionNode root) {
        this.source = source;
        this.root = root;
    }

    /**
     * Evaluates the expression and returns the result as a double.
     */
    public double evaluate(ExpressionContext context) {
        return root.evaluate(context);
    }

    /**
     * Evaluates the expression and returns the result as a boolean.
     * Non-zero values are considered true.
     */
    public boolean evaluateBoolean(ExpressionContext context) {
        return root.evaluate(context) != 0.0;
    }

    /**
     * Evaluates the expression and returns the result as a float.
     */
    public float evaluateFloat(ExpressionContext context) {
        return (float) root.evaluate(context);
    }

    /**
     * Returns the original source string of this expression.
     */
    public String getSource() {
        return source;
    }

    /**
     * Returns the root AST node for debugging purposes.
     */
    public ExpressionNode getRoot() {
        return root;
    }

    @Override
    public String toString() {
        return "Expression{" + source + "}";
    }
}

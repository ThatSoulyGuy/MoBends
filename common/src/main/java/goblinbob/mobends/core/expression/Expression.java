package goblinbob.mobends.core.expression;

import goblinbob.mobends.core.expression.ast.ExpressionNode;

public class Expression {
    private final String source;
    private final ExpressionNode root;

    Expression(String source, ExpressionNode root) {
        this.source = source;
        this.root = root;
    }

    public double evaluate(ExpressionContext context) {
        return root.evaluate(context);
    }

    public boolean evaluateBoolean(ExpressionContext context) {
        return root.evaluate(context) != 0.0;
    }

    public float evaluateFloat(ExpressionContext context) {
        return (float) root.evaluate(context);
    }

    public String getSource() {
        return source;
    }

    public ExpressionNode getRoot() {
        return root;
    }

    @Override
    public String toString() {
        return "Expression{" + source + "}";
    }
}

package goblinbob.mobends.core.expression.ast;

import goblinbob.mobends.core.expression.ExpressionContext;

public class VariableNode implements ExpressionNode {
    private final String name;

    public VariableNode(String name) {
        this.name = name;
    }

    @Override
    public double evaluate(ExpressionContext context) {
        return context.getVariable(name);
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}

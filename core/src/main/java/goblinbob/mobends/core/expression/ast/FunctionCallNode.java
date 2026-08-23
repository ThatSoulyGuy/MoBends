package goblinbob.mobends.core.expression.ast;

import goblinbob.mobends.core.expression.ExpressionContext;
import goblinbob.mobends.core.expression.functions.ExpressionFunction;
import goblinbob.mobends.core.expression.functions.FunctionRegistry;

import java.util.List;
import java.util.stream.Collectors;

public class FunctionCallNode implements ExpressionNode {
    private final String functionName;
    private final List<ExpressionNode> arguments;
    private final ExpressionFunction function;

    public FunctionCallNode(String functionName, List<ExpressionNode> arguments) {
        this.functionName = functionName;
        this.arguments = arguments;
        this.function = FunctionRegistry.getFunction(functionName);
    }

    @Override
    public double evaluate(ExpressionContext context) {
        double[] args = new double[arguments.size()];
        for (int i = 0; i < arguments.size(); i++) {
            args[i] = arguments.get(i).evaluate(context);
        }
        return function.apply(args);
    }

    @Override
    public boolean isConstant() {
        if (!function.isPure()) {
            return false;
        }
        for (ExpressionNode arg : arguments) {
            if (!arg.isConstant()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ExpressionNode optimize() {
        boolean allConstant = function.isPure();
        List<ExpressionNode> optimizedArgs = arguments.stream()
                .map(ExpressionNode::optimize)
                .collect(Collectors.toList());

        for (ExpressionNode arg : optimizedArgs) {
            if (!arg.isConstant()) {
                allConstant = false;
                break;
            }
        }

        if (allConstant) {
            return new LiteralNode(new FunctionCallNode(functionName, optimizedArgs).evaluate(ExpressionContext.CONSTANT_FOLDING));
        }

        boolean changed = false;
        for (int i = 0; i < arguments.size(); i++) {
            if (optimizedArgs.get(i) != arguments.get(i)) {
                changed = true;
                break;
            }
        }

        if (changed) {
            return new FunctionCallNode(functionName, optimizedArgs);
        }
        return this;
    }

    public String getFunctionName() {
        return functionName;
    }

    public List<ExpressionNode> getArguments() {
        return arguments;
    }

    @Override
    public String toString() {
        return functionName + "(" +
                arguments.stream().map(Object::toString).collect(Collectors.joining(", ")) +
                ")";
    }
}

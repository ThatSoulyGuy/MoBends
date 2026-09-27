package goblinbob.mobends.core.expression.functions;

import java.util.function.Function;

public record ExpressionFunction(int minArgs, int maxArgs, boolean pure, Function<double[], Double> impl) {
    public double apply(double[] args) {
        return impl.apply(args);
    }

    static ExpressionFunction of(int argCount, Function<double[], Double> impl) {
        return new ExpressionFunction(argCount, argCount, true, impl);
    }

    static ExpressionFunction of(int minArgs, int maxArgs, Function<double[], Double> impl) {
        return new ExpressionFunction(minArgs, maxArgs, true, impl);
    }

    static ExpressionFunction impure(int minArgs, int maxArgs, Function<double[], Double> impl) {
        return new ExpressionFunction(minArgs, maxArgs, false, impl);
    }
}

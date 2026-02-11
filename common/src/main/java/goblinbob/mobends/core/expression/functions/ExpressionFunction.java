package goblinbob.mobends.core.expression.functions;

/**
 * A function that can be called in expressions.
 */
public interface ExpressionFunction {
    /**
     * Applies the function to the given arguments.
     */
    double apply(double[] args);

    /**
     * Returns the minimum number of arguments this function accepts.
     */
    int getMinArgs();

    /**
     * Returns the maximum number of arguments this function accepts.
     */
    int getMaxArgs();

    /**
     * Returns true if this function is pure (deterministic, no side effects).
     * Pure functions can be evaluated at compile time if all arguments are constants.
     */
    default boolean isPure() {
        return true;
    }

    /**
     * Creates a function with a fixed number of arguments.
     */
    static ExpressionFunction of(int argCount, java.util.function.Function<double[], Double> impl) {
        return new ExpressionFunction() {
            @Override
            public double apply(double[] args) {
                return impl.apply(args);
            }

            @Override
            public int getMinArgs() {
                return argCount;
            }

            @Override
            public int getMaxArgs() {
                return argCount;
            }
        };
    }

    /**
     * Creates a function with a variable number of arguments.
     */
    static ExpressionFunction of(int minArgs, int maxArgs, java.util.function.Function<double[], Double> impl) {
        return new ExpressionFunction() {
            @Override
            public double apply(double[] args) {
                return impl.apply(args);
            }

            @Override
            public int getMinArgs() {
                return minArgs;
            }

            @Override
            public int getMaxArgs() {
                return maxArgs;
            }
        };
    }

    /**
     * Creates a non-pure function (like random).
     */
    static ExpressionFunction impure(int argCount, java.util.function.Function<double[], Double> impl) {
        return new ExpressionFunction() {
            @Override
            public double apply(double[] args) {
                return impl.apply(args);
            }

            @Override
            public int getMinArgs() {
                return argCount;
            }

            @Override
            public int getMaxArgs() {
                return argCount;
            }

            @Override
            public boolean isPure() {
                return false;
            }
        };
    }

    /**
     * Creates a non-pure function with variable arguments.
     */
    static ExpressionFunction impure(int minArgs, int maxArgs, java.util.function.Function<double[], Double> impl) {
        return new ExpressionFunction() {
            @Override
            public double apply(double[] args) {
                return impl.apply(args);
            }

            @Override
            public int getMinArgs() {
                return minArgs;
            }

            @Override
            public int getMaxArgs() {
                return maxArgs;
            }

            @Override
            public boolean isPure() {
                return false;
            }
        };
    }
}

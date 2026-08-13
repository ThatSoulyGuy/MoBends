package goblinbob.mobends.core.expression.functions;

public interface ExpressionFunction {
    double apply(double[] args);

    int getMinArgs();

    int getMaxArgs();

    default boolean isPure() {
        return true;
    }

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

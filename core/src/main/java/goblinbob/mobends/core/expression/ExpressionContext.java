package goblinbob.mobends.core.expression;

public interface ExpressionContext {
    double getVariable(String name);

    ExpressionContext CONSTANT_FOLDING = new ExpressionContext() {
        @Override
        public double getVariable(String name) {
            throw new ExpressionException(
                    "Variable '" + name + "' was read while constant-folding. A node that reports "
                            + "isConstant() == true must not depend on the evaluation context.",
                    name, 0);
        }
    };
}

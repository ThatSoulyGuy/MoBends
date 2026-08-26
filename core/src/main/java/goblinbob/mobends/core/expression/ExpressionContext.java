package goblinbob.mobends.core.expression;

public interface ExpressionContext {
    double getVariable(String name);

    default boolean hasVariable(String name) {
        return true;
    }

    ExpressionContext CONSTANT_FOLDING = new ExpressionContext() {
        @Override
        public double getVariable(String name) {
            throw new ExpressionException(
                    "Variable '" + name + "' was read while constant-folding. A node that reports "
                            + "isConstant() == true must not depend on the evaluation context.",
                    name, 0);
        }

        @Override
        public boolean hasVariable(String name) {
            return false;
        }
    };
}

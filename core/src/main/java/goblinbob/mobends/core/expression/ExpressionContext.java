package goblinbob.mobends.core.expression;

public interface ExpressionContext {
    double getVariable(String name);

    default boolean hasVariable(String name) {
        return true;
    }

    /**
     * The context used while constant-folding at parse time, where by definition no variable may
     * be read.
     *
     * <p>Folding previously passed {@code null} here. That was safe only by coincidence: every
     * node that reads the context also reports {@code isConstant() == false}, so no folded
     * subtree ever touched it. A new node type that was constant but consulted the context would
     * have thrown a bare NullPointerException from inside the parser, pointing at the wrong
     * thing entirely. This turns that into a message naming the variable and the reason.
     *
     * <p>Note this is only for folding — {@code evaluate(null)} remains valid for callers
     * evaluating an expression already known to be constant.
     */
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

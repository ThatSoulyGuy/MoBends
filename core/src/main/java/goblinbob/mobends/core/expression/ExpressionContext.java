package goblinbob.mobends.core.expression;

public interface ExpressionContext {
    double getVariable(String name);

    default boolean hasVariable(String name) {
        return true;
    }
}

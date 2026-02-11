package goblinbob.mobends.core.expression;

/**
 * Provides variable values for expression evaluation.
 * Implementations bridge to entity data and other animation state.
 */
public interface ExpressionContext {
    /**
     * Gets the value of a variable by name.
     * Returns 0.0 for unknown variables.
     */
    double getVariable(String name);

    /**
     * Checks if a variable exists in this context.
     */
    default boolean hasVariable(String name) {
        return true; // Default implementation assumes all variables exist
    }
}

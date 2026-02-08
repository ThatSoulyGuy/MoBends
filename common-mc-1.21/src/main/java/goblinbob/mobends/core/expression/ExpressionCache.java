package goblinbob.mobends.core.expression;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton cache for compiled expressions.
 * Expressions are parsed once and reused for efficient evaluation.
 */
public final class ExpressionCache {
    private static final ExpressionCache INSTANCE = new ExpressionCache();
    private final Map<String, Expression> cache = new ConcurrentHashMap<>();

    private ExpressionCache() {}

    public static ExpressionCache getInstance() {
        return INSTANCE;
    }

    /**
     * Gets a compiled expression, parsing it if not already cached.
     * The expression is optimized with constant folding.
     */
    public Expression get(String source) {
        return cache.computeIfAbsent(source, this::compile);
    }

    /**
     * Gets a compiled expression without caching.
     */
    public Expression compile(String source) {
        ExpressionParser parser = new ExpressionParser(source);
        return new Expression(source, parser.parseAndOptimize());
    }

    /**
     * Clears the expression cache.
     * Call this when reloading packs or during development.
     */
    public void clear() {
        cache.clear();
    }

    /**
     * Returns the number of cached expressions.
     */
    public int size() {
        return cache.size();
    }

    /**
     * Checks if an expression is already cached.
     */
    public boolean contains(String source) {
        return cache.containsKey(source);
    }

    /**
     * Removes a specific expression from the cache.
     */
    public void remove(String source) {
        cache.remove(source);
    }

    /**
     * Convenience method to compile and evaluate an expression in one call.
     * Useful for simple one-off evaluations.
     */
    public static double evaluate(String source, ExpressionContext context) {
        return INSTANCE.get(source).evaluate(context);
    }

    /**
     * Convenience method to compile and evaluate as boolean.
     */
    public static boolean evaluateBoolean(String source, ExpressionContext context) {
        return INSTANCE.get(source).evaluateBoolean(context);
    }

    /**
     * Convenience method to compile and evaluate as float.
     */
    public static float evaluateFloat(String source, ExpressionContext context) {
        return INSTANCE.get(source).evaluateFloat(context);
    }
}

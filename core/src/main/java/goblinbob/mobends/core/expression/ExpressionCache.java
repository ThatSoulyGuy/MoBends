package goblinbob.mobends.core.expression;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ExpressionCache {
    private static final ExpressionCache INSTANCE = new ExpressionCache();
    private final Map<String, Expression> cache = new ConcurrentHashMap<>();

    private ExpressionCache() {}

    public static ExpressionCache getInstance() {
        return INSTANCE;
    }

    public Expression get(String source) {
        return cache.computeIfAbsent(source, this::compile);
    }

    public Expression compile(String source) {
        ExpressionParser parser = new ExpressionParser(source);
        return new Expression(source, parser.parseAndOptimize());
    }

    public void remove(String source) {
        cache.remove(source);
    }
}

package goblinbob.mobends.core.expression;

public final class ExpressionMath {

    private ExpressionMath() {}

    public static double safeDivide(double a, double b) {
        return b != 0.0 ? a / b : 0.0;
    }

    public static double safeModulo(double a, double b) {
        return b != 0.0 ? a % b : 0.0;
    }
}

package goblinbob.mobends.core.expression;

/**
 * Arithmetic helpers shared by the operator nodes and the function library.
 *
 * <p>Expression results feed bone rotations and offsets. A NaN or infinity reaching a transform
 * does not fail loudly — it silently corrupts that bone, and every bone parented to it, for as
 * long as the entity is loaded. So division by zero deliberately yields zero here rather than
 * propagating IEEE semantics: a bad pack produces a wrong-looking animation the author can see
 * and fix, instead of an invisible skeleton corruption they cannot.
 *
 * <p>This is a real trade-off and not obviously the right one in general — it is right for this
 * consumer. It lives in one place so the operators and the {@code mod} function cannot drift
 * apart on it.
 */
public final class ExpressionMath {

    private ExpressionMath() {}

    /** {@code a / b}, or 0 when {@code b} is zero. */
    public static double safeDivide(double a, double b) {
        return b != 0.0 ? a / b : 0.0;
    }

    /** {@code a % b}, or 0 when {@code b} is zero. */
    public static double safeModulo(double a, double b) {
        return b != 0.0 ? a % b : 0.0;
    }
}

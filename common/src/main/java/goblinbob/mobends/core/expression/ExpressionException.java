package goblinbob.mobends.core.expression;

/**
 * Exception thrown when an expression cannot be parsed or evaluated.
 */
public class ExpressionException extends RuntimeException {
    private final String expression;
    private final int position;

    public ExpressionException(String message, String expression, int position) {
        super(formatMessage(message, expression, position));
        this.expression = expression;
        this.position = position;
    }

    public ExpressionException(String message, String expression, int position, Throwable cause) {
        super(formatMessage(message, expression, position), cause);
        this.expression = expression;
        this.position = position;
    }

    private static String formatMessage(String message, String expression, int position) {
        StringBuilder sb = new StringBuilder();
        sb.append(message);
        sb.append("\n  Expression: ").append(expression);
        if (position >= 0 && position <= expression.length()) {
            sb.append("\n             ");
            for (int i = 0; i < position; i++) {
                sb.append(' ');
            }
            sb.append('^');
        }
        return sb.toString();
    }

    public String getExpression() {
        return expression;
    }

    public int getPosition() {
        return position;
    }
}

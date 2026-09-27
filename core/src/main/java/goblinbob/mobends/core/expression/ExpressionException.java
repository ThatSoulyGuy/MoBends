package goblinbob.mobends.core.expression;

public class ExpressionException extends RuntimeException {
    public ExpressionException(String message, String expression, int position) {
        super(formatMessage(message, expression, position));
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
}

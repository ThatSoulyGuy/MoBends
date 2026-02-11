package goblinbob.mobends.core.expression;

/**
 * A token produced by the expression tokenizer.
 */
public record Token(TokenType type, String value, int position) {
    @Override
    public String toString() {
        return type + "(" + value + ")@" + position;
    }
}

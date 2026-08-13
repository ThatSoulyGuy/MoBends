package goblinbob.mobends.core.expression;

public record Token(TokenType type, String value, int position) {
    @Override
    public String toString() {
        return type + "(" + value + ")@" + position;
    }
}

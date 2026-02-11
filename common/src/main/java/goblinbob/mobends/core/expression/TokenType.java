package goblinbob.mobends.core.expression;

/**
 * Types of tokens in expressions.
 */
public enum TokenType {
    NUMBER,         // 3.14, -5, 0
    IDENTIFIER,     // ticks, sin, health
    PLUS,           // +
    MINUS,          // -
    STAR,           // *
    SLASH,          // /
    PERCENT,        // %
    CARET,          // ^
    LPAREN,         // (
    RPAREN,         // )
    COMMA,          // ,
    QUESTION,       // ?
    COLON,          // :
    LESS,           // <
    GREATER,        // >
    LESS_EQUAL,     // <=
    GREATER_EQUAL,  // >=
    EQUAL,          // ==
    NOT_EQUAL,      // !=
    AND,            // &&
    OR,             // ||
    NOT,            // !
    EOF             // End of expression
}

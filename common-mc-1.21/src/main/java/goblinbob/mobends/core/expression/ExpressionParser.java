package goblinbob.mobends.core.expression;

import goblinbob.mobends.core.expression.ast.*;
import goblinbob.mobends.core.expression.functions.FunctionRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * Recursive descent parser for mathematical expressions.
 *
 * Grammar (by precedence, lowest to highest):
 *   1. Ternary:        ?:
 *   2. Logical Or:     ||
 *   3. Logical And:    &&
 *   4. Equality:       ==, !=
 *   5. Comparison:     <, >, <=, >=
 *   6. Additive:       +, -
 *   7. Multiplicative: *, /, %
 *   8. Power:          ^
 *   9. Unary:          -, !
 *  10. Primary:        literals, variables, function calls, parentheses
 */
public class ExpressionParser {
    private final String source;
    private final List<Token> tokens;
    private int current;

    public ExpressionParser(String source) {
        this.source = source;
        this.tokens = new Tokenizer(source).tokenize();
        this.current = 0;
    }

    /**
     * Parses the expression and returns the root AST node.
     */
    public ExpressionNode parse() {
        ExpressionNode result = parseTernary();

        if (!isAtEnd()) {
            Token tok = peek();
            throw new ExpressionException("Unexpected token: " + tok.value(), source, tok.position());
        }

        return result;
    }

    /**
     * Parses the expression with optimization (constant folding).
     */
    public ExpressionNode parseAndOptimize() {
        return parse().optimize();
    }

    // Level 1: Ternary (? :)
    private ExpressionNode parseTernary() {
        ExpressionNode condition = parseOr();

        if (match(TokenType.QUESTION)) {
            ExpressionNode thenBranch = parseTernary();
            consume(TokenType.COLON, "Expected ':' in ternary expression");
            ExpressionNode elseBranch = parseTernary();
            return new TernaryNode(condition, thenBranch, elseBranch);
        }

        return condition;
    }

    // Level 2: Logical Or (||)
    private ExpressionNode parseOr() {
        ExpressionNode left = parseAnd();

        while (match(TokenType.OR)) {
            ExpressionNode right = parseAnd();
            left = new BinaryOpNode(left, BinaryOpNode.Operator.OR, right);
        }

        return left;
    }

    // Level 3: Logical And (&&)
    private ExpressionNode parseAnd() {
        ExpressionNode left = parseEquality();

        while (match(TokenType.AND)) {
            ExpressionNode right = parseEquality();
            left = new BinaryOpNode(left, BinaryOpNode.Operator.AND, right);
        }

        return left;
    }

    // Level 4: Equality (==, !=)
    private ExpressionNode parseEquality() {
        ExpressionNode left = parseComparison();

        while (true) {
            if (match(TokenType.EQUAL)) {
                ExpressionNode right = parseComparison();
                left = new BinaryOpNode(left, BinaryOpNode.Operator.EQUAL, right);
            } else if (match(TokenType.NOT_EQUAL)) {
                ExpressionNode right = parseComparison();
                left = new BinaryOpNode(left, BinaryOpNode.Operator.NOT_EQUAL, right);
            } else {
                break;
            }
        }

        return left;
    }

    // Level 5: Comparison (<, >, <=, >=)
    private ExpressionNode parseComparison() {
        ExpressionNode left = parseAdditive();

        while (true) {
            if (match(TokenType.LESS)) {
                ExpressionNode right = parseAdditive();
                left = new BinaryOpNode(left, BinaryOpNode.Operator.LESS_THAN, right);
            } else if (match(TokenType.GREATER)) {
                ExpressionNode right = parseAdditive();
                left = new BinaryOpNode(left, BinaryOpNode.Operator.GREATER_THAN, right);
            } else if (match(TokenType.LESS_EQUAL)) {
                ExpressionNode right = parseAdditive();
                left = new BinaryOpNode(left, BinaryOpNode.Operator.LESS_EQUAL, right);
            } else if (match(TokenType.GREATER_EQUAL)) {
                ExpressionNode right = parseAdditive();
                left = new BinaryOpNode(left, BinaryOpNode.Operator.GREATER_EQUAL, right);
            } else {
                break;
            }
        }

        return left;
    }

    // Level 6: Additive (+, -)
    private ExpressionNode parseAdditive() {
        ExpressionNode left = parseMultiplicative();

        while (true) {
            if (match(TokenType.PLUS)) {
                ExpressionNode right = parseMultiplicative();
                left = new BinaryOpNode(left, BinaryOpNode.Operator.ADD, right);
            } else if (match(TokenType.MINUS)) {
                ExpressionNode right = parseMultiplicative();
                left = new BinaryOpNode(left, BinaryOpNode.Operator.SUBTRACT, right);
            } else {
                break;
            }
        }

        return left;
    }

    // Level 7: Multiplicative (*, /, %)
    private ExpressionNode parseMultiplicative() {
        ExpressionNode left = parsePower();

        while (true) {
            if (match(TokenType.STAR)) {
                ExpressionNode right = parsePower();
                left = new BinaryOpNode(left, BinaryOpNode.Operator.MULTIPLY, right);
            } else if (match(TokenType.SLASH)) {
                ExpressionNode right = parsePower();
                left = new BinaryOpNode(left, BinaryOpNode.Operator.DIVIDE, right);
            } else if (match(TokenType.PERCENT)) {
                ExpressionNode right = parsePower();
                left = new BinaryOpNode(left, BinaryOpNode.Operator.MODULO, right);
            } else {
                break;
            }
        }

        return left;
    }

    // Level 8: Power (^) - right associative
    private ExpressionNode parsePower() {
        ExpressionNode left = parseUnary();

        if (match(TokenType.CARET)) {
            ExpressionNode right = parsePower(); // Right associative
            return new BinaryOpNode(left, BinaryOpNode.Operator.POWER, right);
        }

        return left;
    }

    // Level 9: Unary (-, !)
    private ExpressionNode parseUnary() {
        if (match(TokenType.MINUS)) {
            ExpressionNode operand = parseUnary();
            return new UnaryOpNode(UnaryOpNode.Operator.NEGATE, operand);
        }
        if (match(TokenType.NOT)) {
            ExpressionNode operand = parseUnary();
            return new UnaryOpNode(UnaryOpNode.Operator.NOT, operand);
        }

        return parsePrimary();
    }

    // Level 10: Primary (literals, variables, function calls, parentheses)
    private ExpressionNode parsePrimary() {
        // Number literal
        if (check(TokenType.NUMBER)) {
            Token token = advance();
            try {
                double value = Double.parseDouble(token.value());
                return new LiteralNode(value);
            } catch (NumberFormatException e) {
                throw new ExpressionException("Invalid number: " + token.value(), source, token.position());
            }
        }

        // Identifier (variable or function call)
        if (check(TokenType.IDENTIFIER)) {
            Token token = advance();
            String name = token.value();

            // Check for function call
            if (match(TokenType.LPAREN)) {
                return parseFunctionCall(name, token.position());
            }

            // Built-in constants
            if (name.equals("PI")) {
                return new LiteralNode(Math.PI);
            }
            if (name.equals("E")) {
                return new LiteralNode(Math.E);
            }
            if (name.equalsIgnoreCase("true")) {
                return new LiteralNode(1.0);
            }
            if (name.equalsIgnoreCase("false")) {
                return new LiteralNode(0.0);
            }

            // Variable reference
            return new VariableNode(name);
        }

        // Parenthesized expression
        if (match(TokenType.LPAREN)) {
            ExpressionNode expr = parseTernary();
            consume(TokenType.RPAREN, "Expected ')' after expression");
            return expr;
        }

        Token tok = peek();
        throw new ExpressionException("Expected expression, got: " + tok.type(), source, tok.position());
    }

    private ExpressionNode parseFunctionCall(String name, int position) {
        List<ExpressionNode> arguments = new ArrayList<>();

        // Empty argument list
        if (!check(TokenType.RPAREN)) {
            do {
                arguments.add(parseTernary());
            } while (match(TokenType.COMMA));
        }

        consume(TokenType.RPAREN, "Expected ')' after function arguments");

        // Validate function exists and argument count
        if (!FunctionRegistry.hasFunction(name)) {
            throw new ExpressionException("Unknown function: " + name, source, position);
        }

        var func = FunctionRegistry.getFunction(name);
        if (arguments.size() < func.getMinArgs()) {
            throw new ExpressionException(
                    "Function '" + name + "' requires at least " + func.getMinArgs() + " arguments, got " + arguments.size(),
                    source, position
            );
        }
        if (arguments.size() > func.getMaxArgs()) {
            throw new ExpressionException(
                    "Function '" + name + "' accepts at most " + func.getMaxArgs() + " arguments, got " + arguments.size(),
                    source, position
            );
        }

        return new FunctionCallNode(name, arguments);
    }

    // Helper methods
    private boolean match(TokenType type) {
        if (check(type)) {
            advance();
            return true;
        }
        return false;
    }

    private boolean check(TokenType type) {
        return !isAtEnd() && peek().type() == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private boolean isAtEnd() {
        return peek().type() == TokenType.EOF;
    }

    private void consume(TokenType type, String message) {
        if (check(type)) {
            advance();
            return;
        }
        Token tok = peek();
        throw new ExpressionException(message, source, tok.position());
    }
}

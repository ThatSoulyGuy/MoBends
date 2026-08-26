package goblinbob.mobends.core.expression;

import goblinbob.mobends.core.expression.ast.*;
import goblinbob.mobends.core.expression.functions.FunctionRegistry;

import java.util.ArrayList;
import java.util.List;

public class ExpressionParser {
    private final String source;
    private final List<Token> tokens;
    private int current;

    public ExpressionParser(String source) {
        this.source = source;
        this.tokens = new Tokenizer(source).tokenize();
        this.current = 0;
    }

    public ExpressionNode parse() {
        ExpressionNode result = parseTernary();

        if (!isAtEnd()) {
            Token tok = peek();
            throw new ExpressionException("Unexpected token: " + tok.value(), source, tok.position());
        }

        return result;
    }

    public ExpressionNode parseAndOptimize() {
        return parse().optimize();
    }

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

    private ExpressionNode parseOr() {
        ExpressionNode left = parseAnd();

        while (match(TokenType.OR)) {
            ExpressionNode right = parseAnd();
            left = new BinaryOpNode(left, BinaryOpNode.Operator.OR, right);
        }

        return left;
    }

    private ExpressionNode parseAnd() {
        ExpressionNode left = parseEquality();

        while (match(TokenType.AND)) {
            ExpressionNode right = parseEquality();
            left = new BinaryOpNode(left, BinaryOpNode.Operator.AND, right);
        }

        return left;
    }

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

    private ExpressionNode parsePower() {
        ExpressionNode left = parseUnary();

        if (match(TokenType.CARET)) {
            ExpressionNode right = parsePower();
            return new BinaryOpNode(left, BinaryOpNode.Operator.POWER, right);
        }

        return left;
    }

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

    private ExpressionNode parsePrimary() {
        if (check(TokenType.NUMBER)) {
            Token token = advance();
            try {
                double value = Double.parseDouble(token.value());
                return new LiteralNode(value);
            } catch (NumberFormatException e) {
                throw new ExpressionException("Invalid number: " + token.value(), source, token.position());
            }
        }

        if (check(TokenType.IDENTIFIER)) {
            Token token = advance();
            String name = token.value();

            if (match(TokenType.LPAREN)) {
                return parseFunctionCall(name, token.position());
            }

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

            return new VariableNode(name);
        }

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

        if (!check(TokenType.RPAREN)) {
            do {
                arguments.add(parseTernary());
            } while (match(TokenType.COMMA));
        }

        consume(TokenType.RPAREN, "Expected ')' after function arguments");

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

        if (name.equalsIgnoreCase("if")) {
            return new TernaryNode(arguments.get(0), arguments.get(1), arguments.get(2));
        }

        return new FunctionCallNode(name, arguments);
    }

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

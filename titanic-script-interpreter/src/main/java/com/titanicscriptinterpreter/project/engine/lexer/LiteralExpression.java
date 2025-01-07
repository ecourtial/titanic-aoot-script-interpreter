package com.titanicscriptinterpreter.project.engine.lexer;

public class LiteralExpression implements Expression {
    private final TokenType type;
    private final String literal;

    public LiteralExpression(TokenType type, String literal) {
        this.type = type;
        this.literal = literal;
    }

    public TokenType getLiteralType() { return type; }
    public String getLiteral() { return literal; }

    @Override
    public String toString() {
        return "LiteralExpression{" + type + "=" + literal + "}";
    }
}
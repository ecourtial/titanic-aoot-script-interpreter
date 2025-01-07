package com.titanicscriptinterpreter.project.engine.lexer;

public class Token {
    private final TokenType type;
    private final String text;
    private final int line;

    public Token(TokenType type, String text, int line) {
        this.type = type;
        this.text = text;
        this.line = line;
    }

    public TokenType getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public int getLine() {
        return line;
    }

    @Override
    public String toString() {
        return "Token{" +
                "type=" + type +
                ", text='" + text + '\'' +
                ", line=" + line +
                '}';
    }
}


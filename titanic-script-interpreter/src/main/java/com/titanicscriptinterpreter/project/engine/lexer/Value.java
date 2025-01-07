package com.titanicscriptinterpreter.project.engine.lexer;

public class Value {
    private final ValueType type;
    private final String literal;

    public Value(ValueType type, String literal) {
        this.type = type;
        this.literal = literal;
    }

    public ValueType getType() {
        return type;
    }

    public String getLiteral() {
        return literal;
    }

    @Override
    public String toString() {
        return literal + "(" + type + ")";
    }
}

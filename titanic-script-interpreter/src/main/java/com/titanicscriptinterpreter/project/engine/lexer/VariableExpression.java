package com.titanicscriptinterpreter.project.engine.lexer;

public class VariableExpression implements Expression {
    private final String name;

    public VariableExpression(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    @Override
    public String toString() {
        return "VariableExpression{" + name + "}";
    }
}

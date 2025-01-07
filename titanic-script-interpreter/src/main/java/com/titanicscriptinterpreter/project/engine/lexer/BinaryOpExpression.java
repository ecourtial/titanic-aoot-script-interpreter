package com.titanicscriptinterpreter.project.engine.lexer;

public class BinaryOpExpression implements Expression {
    private final Expression left;
    private final String operator;
    private final Expression right;

    public BinaryOpExpression(Expression left, String operator, Expression right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    public Expression getLeft() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Expression getRight() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getOperator() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}


package com.titanicscriptinterpreter.project.engine.lexer;

public class Condition implements Expression{

    private final Expression leftExpr;
    private final String operator;  // For instance: "="
    private final Expression rightExpr;

    public Condition(Expression leftExpr, String operator, Expression rightExpr) {
        this.leftExpr = leftExpr;
        this.operator = operator;
        this.rightExpr = rightExpr;
    }

    public Expression getLeftExpr() {
        return leftExpr;
    }

    public String getOperator() {
        return operator;
    }

    public Expression getRightExpr() {
        return rightExpr;
    }

    @Override
    public String toString() {
        return "Condition(" + leftExpr + " " + operator + " " + rightExpr + ")";
    }
}

package com.titanicscriptinterpreter.project.engine.statements;

import com.titanicscriptinterpreter.project.engine.lexer.Expression;

public class ReturnStatement implements Statement {
    private final Expression returnValue;

    public ReturnStatement(Expression returnValue) {
        this.returnValue = returnValue;
    }

    public Expression getReturnValue() {
        return returnValue;
    }

    @Override
    public String toString() {
        return "ReturnStatement{" + returnValue + "}";
    }
}


package com.titanicscriptinterpreter.project.engine.lexer;

import java.util.List;

public class FunctionCallExpression implements Expression {
    private final String functionName;
    private final List<Expression> arguments;

    public FunctionCallExpression(String functionName, List<Expression> arguments) {
        this.functionName = functionName;
        this.arguments = arguments;
    }

    public String getFunctionName() { return functionName; }
    public List<Expression> getArguments() { return arguments; }

    @Override
    public String toString() {
        return "FunctionCallExpression{" + functionName + arguments + "}";
    }
}


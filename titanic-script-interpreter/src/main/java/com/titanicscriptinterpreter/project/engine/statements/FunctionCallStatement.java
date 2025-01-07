package com.titanicscriptinterpreter.project.engine.statements;

import java.util.List;
import com.titanicscriptinterpreter.project.engine.lexer.Expression;

public class FunctionCallStatement implements Statement {

    private final String functionName;
    private final List<Expression> arguments;

    public FunctionCallStatement(String functionName, List<Expression> arguments) {
        this.functionName = functionName;
        this.arguments = arguments;
    }

    public String getFunctionName() {
        return functionName;
    }

    /**
     * @return The list of argument expressions to be evaluated at runtime.
     */
    public List<Expression> getArguments() {
        return arguments;
    }

    @Override
    public String toString() {
        return "FunctionCallStatement{" +
                "functionName='" + functionName + '\'' +
                ", arguments=" + arguments +
                '}';
    }
}

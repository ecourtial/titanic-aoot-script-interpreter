package com.titanicscriptinterpreter.project.engine.statements;

import com.titanicscriptinterpreter.project.engine.interpreter.ScriptContext;
import java.util.List;

/**
 * Represents a function declaration in the script
 */
public class FunctionDeclarationStatement implements Statement {

    private final String functionName;
    private final List<String> parameters;
    private final List<Statement> body;

    // Store the context under which this function was declared.
    private final ScriptContext creationContext;

    public FunctionDeclarationStatement(String functionName,
            List<String> parameters,
            List<Statement> body,
            ScriptContext creationContext) {
        this.functionName = functionName;
        this.parameters = parameters;
        this.body = body;
        this.creationContext = creationContext;
    }

    public FunctionDeclarationStatement(String functionName,
            List<String> parameters,
            List<Statement> body) {
        this(functionName, parameters, body, null);
    }

    public String getFunctionName() {
        return functionName;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public List<Statement> getBody() {
        return body;
    }

    public ScriptContext getCreationContext() {
        return creationContext;
    }

    @Override
    public String toString() {
        return "FunctionDeclarationStatement{"
                + "functionName='" + functionName + '\''
                + ", parameters=" + parameters
                + ", body=" + body
                + ", creationContext=" + creationContext
                + '}';
    }
}

package com.titanicscriptinterpreter.project.engine.statements;

import com.titanicscriptinterpreter.project.engine.lexer.Expression;

public class AssignmentStatement implements Statement {
    private final String variableName;
    private final Expression rightHandSide;

    public AssignmentStatement(String variableName, Expression rightHandSide) {
        this.variableName = variableName;
        this.rightHandSide = rightHandSide;
    }

    public String getVariableName() { return variableName; }
    public Expression getRightHandSide() { return rightHandSide; }

    @Override
    public String toString() {
        return variableName + " = " + rightHandSide;
    }
}

package com.titanicscriptinterpreter.project.engine.statements;

import java.util.List;

public class LocalStatement implements Statement {

    private final List<String> variableNames;

    public LocalStatement(List<String> variableNames) {
        this.variableNames = variableNames;
    }

    public List<String> getVariableNames() {
        return variableNames;
    }

    @Override
    public String toString() {
        return "LocalStatement{vars=" + variableNames + "}";
    }
}

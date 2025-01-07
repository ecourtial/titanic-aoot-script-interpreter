package com.titanicscriptinterpreter.project.engine.statements;

import java.util.List;

public class GlobalStatement implements Statement {
    private final List<String> variableNames;

    public GlobalStatement(List<String> variableNames) {
        this.variableNames = variableNames;
    }

    public List<String> getVariableNames() {
        return variableNames;
    }

    @Override
    public String toString() {
        return "GlobalStatement{variableNames=" + variableNames + "}";
    }
}


package com.titanicscriptinterpreter.project.engine.statements;

import com.titanicscriptinterpreter.project.engine.lexer.Condition;
import java.util.List;

public class IfStatement implements Statement {

    private final Condition condition;
    private final List<Statement> thenStatements;
    private final List<Statement> elseStatements;  // Can be empty if no else

    public IfStatement(Condition condition,
            List<Statement> thenStatements,
            List<Statement> elseStatements) {
        this.condition = condition;
        this.thenStatements = thenStatements;
        this.elseStatements = elseStatements;
    }

    public Condition getCondition() {
        return condition;
    }

    public List<Statement> getThenStatements() {
        return thenStatements;
    }

    public List<Statement> getElseStatements() {
        return elseStatements;
    }

    @Override
    public String toString() {
        return "IfStatement{"
                + "condition=" + condition
                + ", thenStatements=" + thenStatements
                + ", elseStatements=" + elseStatements
                + '}';
    }
}

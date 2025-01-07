package com.titanicscriptinterpreter.project.engine.lexer;

import com.titanicscriptinterpreter.project.engine.statements.Statement;
import java.util.List;

public class CaseBlock {

    private final Expression value;           // The case value expression
    private final List<Statement> statements; // The statements in that case

    public CaseBlock(Expression value, List<Statement> statements) {
        this.value = value;
        this.statements = statements;
    }

    public Expression getValue() {
        return value;
    }

    public List<Statement> getStatements() {
        return statements;
    }

    @Override
    public String toString() {
        return "CaseBlock{value=" + value + ", statements=" + statements + "}";
    }
}

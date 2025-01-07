package com.titanicscriptinterpreter.project.engine.statements;

import com.titanicscriptinterpreter.project.engine.lexer.Condition;
import java.util.List;

public class WhileStatement implements Statement {
    private final Condition condition;
    private final List<Statement> body;

    public WhileStatement(Condition condition, List<Statement> body) {
        this.condition = condition;
        this.body = body;
    }

    public Condition getCondition() { return condition; }
    public List<Statement> getBody() { return body; }

    @Override
    public String toString() {
        return "WhileStatement{ condition=" + condition + ", body=" + body + "}";
    }
}

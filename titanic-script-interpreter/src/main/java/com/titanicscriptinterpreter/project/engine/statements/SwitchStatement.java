package com.titanicscriptinterpreter.project.engine.statements;

import com.titanicscriptinterpreter.project.engine.lexer.CaseBlock;
import com.titanicscriptinterpreter.project.engine.lexer.Expression;
import java.util.List;

public class SwitchStatement implements Statement {

    private final Expression switchExpression;
    private final List<CaseBlock> cases;

    public SwitchStatement(Expression switchExpression, List<CaseBlock> cases) {
        this.switchExpression = switchExpression;
        this.cases = cases;
    }

    public Expression getSwitchExpression() {
        return switchExpression;
    }

    public List<CaseBlock> getCases() {
        return cases;
    }

    @Override
    public String toString() {
        return "SwitchStatement{ expression=" + switchExpression + ", cases=" + cases + "}";
    }
}

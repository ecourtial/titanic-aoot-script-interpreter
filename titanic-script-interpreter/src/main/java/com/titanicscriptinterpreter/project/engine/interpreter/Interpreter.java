package com.titanicscriptinterpreter.project.engine.interpreter;

import com.titanicscriptinterpreter.project.engine.Cli;
import com.titanicscriptinterpreter.project.engine.lexer.BinaryOpExpression;
import com.titanicscriptinterpreter.project.engine.lexer.CaseBlock;
import com.titanicscriptinterpreter.project.engine.lexer.Condition;
import com.titanicscriptinterpreter.project.engine.lexer.Expression;
import com.titanicscriptinterpreter.project.engine.lexer.FunctionCallExpression;
import com.titanicscriptinterpreter.project.engine.lexer.LiteralExpression;
import com.titanicscriptinterpreter.project.engine.lexer.VariableExpression;
import com.titanicscriptinterpreter.project.engine.statements.AssignmentStatement;
import com.titanicscriptinterpreter.project.engine.statements.ExitCodeStatement;
import com.titanicscriptinterpreter.project.engine.statements.FunctionCallStatement;
import com.titanicscriptinterpreter.project.engine.statements.FunctionDeclarationStatement;
import com.titanicscriptinterpreter.project.engine.statements.GlobalStatement;
import com.titanicscriptinterpreter.project.engine.statements.IfStatement;
import com.titanicscriptinterpreter.project.engine.statements.LocalStatement;
import com.titanicscriptinterpreter.project.engine.statements.ReturnStatement;
import com.titanicscriptinterpreter.project.engine.statements.Statement;
import com.titanicscriptinterpreter.project.engine.statements.SwitchStatement;
import com.titanicscriptinterpreter.project.engine.statements.WhileStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Interpreter {

    private final ScriptContext context;
    private final Scanner scanner; // Required when you have to dialog with NPC

    public Interpreter(ScriptContext context) {
        this.context = context;
        this.scanner = new Scanner(System.in);
    }
    
    /**
     * Interpret a list of statements in a given context.
     */
    public void interpretStatements(List<Statement> statements, ScriptContext ctx) {
        for (Statement stmt : statements) {
            this.interpretStatement(stmt, ctx);
        }
    }

    /**
     * Interpret a single statement in the given context.
     */
    private void interpretStatement(Statement stmt, ScriptContext ctx) {
        if (stmt instanceof IfStatement ifStatement) {
            this.interpretIfStatement(ifStatement, ctx);
        } else if (stmt instanceof FunctionDeclarationStatement functionDeclarationStatement) {
            this.interpretFunctionDeclarationStatement(functionDeclarationStatement);
        } else if (stmt instanceof FunctionCallStatement functionCallStatement) {
            this.interpretFunctionCallStatement(functionCallStatement, ctx);
        } else if (stmt instanceof GlobalStatement globalStatement) {
            this.interpretGlobalStatement(globalStatement, ctx);
        } else if (stmt instanceof AssignmentStatement assignmentStatement) {
            this.interpretAssignmentStatement(assignmentStatement, ctx);
        } else if (stmt instanceof ExitCodeStatement) {
            this.interpretExitCodeStatement();
        } else if (stmt instanceof SwitchStatement switchStatement) {
            this.interpretSwitchStatement(switchStatement, ctx);
        } else if (stmt instanceof LocalStatement localStatement) {
            this.interpretLocalStatement(localStatement, ctx);
        } else if (stmt instanceof WhileStatement whileStatement) {
            this.interpretWhileStatement(whileStatement, ctx);
        } else if (stmt instanceof ReturnStatement returnStatement) {
            this.interpretReturnStatement(returnStatement);
        } else {
            System.err.println("Unknown statement: " + stmt);
        }
    }
    
    private Object interpretReturnStatement(ReturnStatement stmt) {
        return stmt.getReturnValue();
    }

    /**
     * @TODO this function probably has an issue. We do not parse multi-condition yet.
     */
    private void interpretWhileStatement(WhileStatement stmt, ScriptContext ctx) {
        while (true) {
            // 1) Evaluate the condition
            Object condVal = this.evaluateExpression(stmt.getCondition(), ctx);

            // 2) Convert it to a boolean (assuming some 'toBoolean' helper)
            if (!this.toBoolean(condVal)) {
                // If false, break the while loop
                break;
            }

            // 3) If true, interpret the body statements
            this.interpretStatements(stmt.getBody(), ctx);
            // Then loop back and evaluate the condition again
        }
    }

    private void interpretSwitchStatement(SwitchStatement stmt, ScriptContext ctx) {
        Object switchValue = this.evaluateExpression(stmt.getSwitchExpression(), ctx);

        for (CaseBlock cb : stmt.getCases()) {
            Object caseVal = this.evaluateExpression(cb.getValue(), ctx);

            if (this.equalsOperator(switchValue, caseVal)) {
                this.interpretStatements(cb.getStatements(), ctx);
                break;
            }
        }
    }

    private void interpretExitCodeStatement() {
        throw new ExitScriptException();
    }

    private void interpretAssignmentStatement(AssignmentStatement stmt, ScriptContext ctx) {
        String varName = stmt.getVariableName();
        Expression rhs = stmt.getRightHandSide();

        Object value = this.evaluateExpression(rhs, ctx);

        if (ctx.getRootContext().hasVariable(varName)) { // Global var
            ctx.getRootContext().setVariable(varName, value);
        } else {
            ctx.setVariable(varName, value);
        }
    }

    private void interpretGlobalStatement(GlobalStatement stmt, ScriptContext ctx) {
        // Register each variable name in the global context
        for (String varName : stmt.getVariableNames()) {
            ScriptContext globalCtx = ctx.getRootContext();
            if (!globalCtx.hasVariable(varName)) {
                globalCtx.setVariable(varName, null);
            }
        }
    }

    private void interpretLocalStatement(LocalStatement stmt, ScriptContext ctx) {
        for (String varName : stmt.getVariableNames()) {
            if (!ctx.hasVariable(varName)) {
                ctx.setVariable(varName, 0); // The default value seems to be zero according to what I see in the gym() function
            }
        }
    }

    private void interpretFunctionDeclarationStatement(FunctionDeclarationStatement stmt) {
        String funcName = stmt.getFunctionName();

        // Register in a function table (map) so that future "callFunction(funcName,...)" can find it
        context.registerFunction(funcName, stmt);

        // Help to debug
        System.out.println("Registered function: " + funcName);
    }

    private void interpretIfStatement(IfStatement ifStmt, ScriptContext ctx) {
        Object conditionValue = this.evaluateExpression(ifStmt.getCondition(), ctx);
        boolean isTrue = this.toBoolean(conditionValue);
        if (isTrue) {
            this.interpretStatements(ifStmt.getThenStatements(), ctx);
        } else {
            this.interpretStatements(ifStmt.getElseStatements(), ctx);
        }
    }

    private void interpretFunctionCallStatement(FunctionCallStatement stmt, ScriptContext ctx) {
        String funcName = stmt.getFunctionName();
        List<Expression> argExprs = stmt.getArguments();

        List<Object> argValues = new ArrayList<>();
        for (Expression expr : argExprs) {
            Object val = this.evaluateExpression(expr, ctx);
            argValues.add(val);
        }

        this.callFunction(funcName, argValues, ctx);
    }

    /**
     * Two possibilities:
     * - A function declared in the script.
     * - A function ... not declared in the script (the ones that are "global" to the engine).
     */
    public Object callFunction(String funcName, List<Object> argValues, ScriptContext ctx) {
        // First, try function declared in the script
        FunctionDeclarationStatement funcDecl = context.getFunction(funcName);
        if (funcDecl != null) {
            return this.executeFunction(funcDecl, argValues);
        }

        switch (funcName) {
            case "puppetspeak":
                Cli.outputMessage("Play dialog sound: '" + argValues.get(0) + "'.");
                break;
            case "message":
                Cli.outputMessage("Level designer comment: '" + argValues.get(0) + "'.");
                break;
            case "puppetclear":
                Cli.outputMessage("Hide answers to the character you are talking to.");
                break;
            case "puppetbevel":
                Cli.outputMessage("Add answer option (id '" + argValues.get(1) + "): '" + argValues.get(0) + "'.");
                break;
            case "puppetevent":
                Cli.outputMessage("Enter the answer id: ");
                int answerId = Integer.parseInt(scanner.nextLine());
                Cli.outputNewLine();
                
                return answerId;
            case "spotmovie":
                Cli.outputMessage("Launch video: '" + argValues.get(0) + "'.");
                break;
            case "opentrackfile":
                Cli.outputMessage("Start playing track: '" + argValues.get(0) + "'.");
                break;
            case "closetrackfile":
                Cli.outputMessage("Stop playing track: '" + argValues.get(0) + "'.");
                break;
            case "delay":
                Cli.outputMessage("Adding a delay of: '" + argValues.get(0) + "'.");
                break;
            case "advancephase":
                int currentPhase = (int) ctx.getRootContext().getVariable("phase");
                currentPhase++;
                ctx.getRootContext().setVariable("phase", currentPhase);
                break;
            default:
                System.err.println("Unknown function: " + funcName);
                break;
        }

        return new EmptyFunctionResult();
    }

    /**
     * Executes a function with the given argument
     */
    private Object executeFunction(FunctionDeclarationStatement funcDecl, List<Object> argValues) {
        // Create a new local context or scope
        ScriptContext localContext = new ScriptContext();
        // link the parent context because we want the access to the global context
        localContext.setParent(context);

        // Bind parameters to arguments
        List<String> paramNames = funcDecl.getParameters();
        for (int i = 0; i < paramNames.size(); i++) {
            String paramName = paramNames.get(i);
            Object value = (i < argValues.size()) ? argValues.get(i) : null;
            localContext.setVariable(paramName, value);
        }

        // Interpret the function in this local context
        try {
            this.interpretStatements(funcDecl.getBody(), localContext);
        } catch (ReturnException re) {
            // if your language allows 'return', you might throw a ReturnException to unwind
            return re.getReturnValue();
        }

        // If no return keyword was encountered
        return null;
    }

    private Object evaluateExpression(Expression expr, ScriptContext ctx) {
        if (expr instanceof LiteralExpression literalExpression) {
            return this.interpretLiteral(literalExpression);
        } else if (expr instanceof VariableExpression variableExpression) {
            return this.interpretVariable(variableExpression, ctx);
        } else if (expr instanceof FunctionCallExpression functionCallExpression) {
            return this.interpretFunctionCallExpression(functionCallExpression, ctx);
        } else if (expr instanceof BinaryOpExpression binaryOpExpression) {
            return this.interpretBinaryOpExpression(binaryOpExpression, ctx);
        } else if (expr instanceof Condition condition) {
            return this.evaluateCondition(condition, ctx);
        } else {
            System.err.println("Unknown expression type: " + expr);
            return null;
        }
    }

    /** @TODO incomplete */
    private Object evaluateCondition(Condition cond, ScriptContext ct) {
        Object leftVal = evaluateExpression(cond.getLeftExpr(), ct);
        Object rightVal = evaluateExpression(cond.getRightExpr(), ct);
        String op = cond.getOperator();

        switch (op) {
            case "=":
                return equalsOperator(leftVal, rightVal);
            case "!=":
                return !equalsOperator(leftVal, rightVal);
            case ">":
                return compareGreater(leftVal, rightVal);
            // etc.
        }
        return false;
    }

    /** @TODO probably incomplete */
    private Object interpretLiteral(LiteralExpression litExpr) {
        switch (litExpr.getLiteralType()) {
            case NUMBER:
                String numStr = litExpr.getLiteral();
                if (numStr.contains(".")) {
                    return Double.parseDouble(numStr);
                } else {
                    return Integer.parseInt(numStr);
                }
            case STRING:
                return litExpr.getLiteral();
            case BOOLEAN:
                return Boolean.parseBoolean(litExpr.getLiteral());
        }
        return null;
    }

    private Object interpretVariable(VariableExpression varExpr, ScriptContext ctx) {
        String varName = varExpr.getName();
        return ctx.getVariable(varName);  // or null if not found
    }

    private Object interpretFunctionCallExpression(FunctionCallExpression callExpr, ScriptContext ctx) {
        String funcName = callExpr.getFunctionName();
        List<Expression> argExprs = callExpr.getArguments();

        List<Object> argValues = new ArrayList<>();
        for (Expression argE : argExprs) {
            argValues.add(evaluateExpression(argE, ctx));
        }

        return this.callFunction(funcName, argValues, ctx);
    }

    private Object interpretBinaryOpExpression(BinaryOpExpression binExpr, ScriptContext ctx) {
        Object leftVal = this.evaluateExpression(binExpr.getLeft(), ctx);
        Object rightVal = this.evaluateExpression(binExpr.getRight(), ctx);
        String op = binExpr.getOperator();

        switch (op) {
            case "=":
                return this.equalsOperator(leftVal, rightVal);
            case "!=":
                return !this.equalsOperator(leftVal, rightVal);
            case ">":
                return this.compareGreater(leftVal, rightVal);
            case "<":
                return this.compareLess(leftVal, rightVal);
            case "&":
                return this.toBoolean(leftVal) && toBoolean(rightVal);
            default:
                System.err.println("Unsupported operator: " + op);
                return null;
        }
    }

    private boolean equalsOperator(Object left, Object right) {
        if (left == null && right == null) {
            return true;
        }
        if (left == null) {
            return false;
        }
        return left.equals(right);
    }

    private boolean compareGreater(Object left, Object right) {
        if (left instanceof Number && right instanceof Number) {
            double l = ((Number) left).doubleValue();
            double r = ((Number) right).doubleValue();
            return l > r;
        }
        // False or if something is wrong
        return false;
    }

    private boolean compareLess(Object left, Object right) {
        if (left instanceof Number && right instanceof Number) {
            double l = ((Number) left).doubleValue();
            double r = ((Number) right).doubleValue();
            return l < r;
        }
        return false;
    }

    private boolean toBoolean(Object val) {
        if (val instanceof Boolean) {
            return (Boolean) val;
        } else if (val instanceof Number) {
            return ((Number) val).doubleValue() != 0.0;
        } else if (val == null) {
            return false;
        }
        return true;
    }
}

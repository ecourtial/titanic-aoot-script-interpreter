package com.titanicscriptinterpreter.project.engine.lexer;

import com.titanicscriptinterpreter.project.engine.statements.AssignmentStatement;
import com.titanicscriptinterpreter.project.engine.statements.ExitCodeStatement;
import com.titanicscriptinterpreter.project.engine.statements.Statement;
import com.titanicscriptinterpreter.project.engine.statements.GlobalStatement;
import com.titanicscriptinterpreter.project.engine.statements.IfStatement;
import com.titanicscriptinterpreter.project.engine.statements.FunctionDeclarationStatement;
import com.titanicscriptinterpreter.project.engine.statements.FunctionCallStatement;
import com.titanicscriptinterpreter.project.engine.statements.LocalStatement;
import com.titanicscriptinterpreter.project.engine.statements.ReturnStatement;
import com.titanicscriptinterpreter.project.engine.statements.SwitchStatement;
import com.titanicscriptinterpreter.project.engine.statements.WhileStatement;
import java.util.ArrayList;
import java.util.List;

/**
 * SIDE NOTE: remember that using the advance() function allows you to see what the next instruction is.
 */
public class Parser {

    private final List<Token> tokens;
    private int current = 0;  // pointer to the current token

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    /**
     * Parses the entire list of tokens into a list of Statement objects.
     */
    public List<Statement> parse() {
        List<Statement> statements = new ArrayList<>();

        while (!isAtEnd()) {
            // parse one statement at a time
            Statement stmt = parseStatement();
            if (stmt != null) {
                statements.add(stmt);
            }
        }

        return statements;
    }

    /**
     * Determine which kind of statement we have to process.
     */
    private Statement parseStatement() {
        // Look at the current token
        Token currentToken = peek();

        if (currentToken.getType() == TokenType.KEYWORD) {
            Token token = peek();
            switch (token.getText()) {
                case "code":
                    return parseFunctionDeclarationStatement();
                case "global":
                    return parseGlobalStatement();
                case "local":
                    return parseLocalStatement();
                case "exitcode":
                    return parseExitCodeStatement();
                case "if":
                    return parseIfStatement();
                case "switch":
                    return parseSwitchStatement();
                case "while":
                    return parseWhileStatement();
                case "return":
                    return parseReturnStatement();
                default:
                    // unknown keyword
                    error("Unknown keyword: " + token.getText(), token.getLine());
                    advance(); // skip it @TODO but we should stop parsing here I think
                    return null;
            }
        }

        // If it's an identifier, it might be an assignment or a function call
        if (currentToken.getType() == TokenType.IDENTIFIER) {
            // Look ahead to see if next token is '='
            Token next = lookAhead(1);
            if (next != null && next.getType() == TokenType.SYMBOL && next.getText().equals("=")) {
                return parseAssignmentStatement();
            } else {
                // Maybe it's a function call or something else @TODO fix that
                return parseFunctionCallStatement();
            }
        }

        // BOOM.
        if (currentToken.getType() != TokenType.NEWLINE) {
            error("Cannot parse statement. Unexpected token: " + currentToken, currentToken.getLine());
        }

        advance();
        return null;
    }

    private Statement parseReturnStatement() {
        // consume the 'return' keyword
        advance();

        // parse an expression for the returned value
        Expression expr = parseExpression();
        if (expr == null) {
            // Return without value. I handle that in the Interpreter. But is it the good approach?
        }

        return new ReturnStatement(expr);
    }

    private Statement parseWhileStatement() {
        advance(); // Skip "while"

        // Parse condition
        Condition condition = parseCondition(); // e.g. "f3 = 0"

        // Parse the statements until 'endwhile'
        List<Statement> body = parseBlockStatements(null, "endwhile");

        // Match 'endwhile'
        if (!matchKeyword("endwhile")) {
            error("Missing 'endwhile' after while block", peek().getLine());
            return null;
        }

        return new WhileStatement(condition, body);
    }

    private Statement parseAssignmentStatement() {
        Token leftVarToken = advance(); // Consume the identifier
        String varName = leftVarToken.getText();

        // Next token must be '=' ... so far, because we do not handle different approaches, like with a function, for instance: while isSomethingDone()
        if (!checkSymbol("=")) {
            error("Expected '=' in assignment", peek().getLine());
            return null;
        }
        advance(); // consume '='

        // Now parse the right-hand side as an expression
        Expression rhs = parseExpression();
        if (rhs == null) {
            error("Failed to parse right-hand side expression in assignment", peek().getLine());
            return null;
        }

        return new AssignmentStatement(varName, rhs);
    }

    private Statement parseExitCodeStatement() {
        // Consume the 'exitcode' token
        advance();

        return new ExitCodeStatement();
    }

    private Statement parseFunctionDeclarationStatement() {
        // Consume 'code'
        advance();

        if (!check(TokenType.IDENTIFIER)) {
            error("Expected identifier after 'code'", peek().getLine());
            return null;
        }
        Token funcNameToken = advance();
        String functionName = funcNameToken.getText();

        // Parse optional parameter(s) in the function declaration
        List<String> params = parseParameterListIfAny();

        // Parse the function body until we encounter 'endcode'
        List<Statement> bodyStmts = parseBlockStatements(null, "endcode");

        if (!matchKeyword("endcode")) {
            error("Missing 'endcode' after function declaration", peek().getLine());
            return null;
        }

        return new FunctionDeclarationStatement(functionName, params, bodyStmts);
    }

    /** For declaration of local variables */
    private Statement parseLocalStatement() {
        // Consume 'local' keyword
        advance();

        // parse variable names separated by commas
        List<String> vars = new ArrayList<>();
        while (!isAtEnd()) {
            Token tk = peek();
            // If it's IDENTIFIER, add it to list
            if (check(TokenType.IDENTIFIER)) {
                Token varToken = advance();
                vars.add(varToken.getText());
            } // If it's a comma, skip it and continue
            else if (checkSymbol(",")) {
                advance();
                continue;
            } // Otherwise we might be at end of line or next statement
            else {
                break;
            }
        }

        return new LocalStatement(vars);
    }

    private List<String> parseParameterListIfAny() {
        List<String> params = new ArrayList<>();

        // Check if the next token is '('
        if (checkSymbol("(")) {
            // Consume '('
            advance();

            // Now parse possible comma-separated variables until we encounter ')'
            while (!isAtEnd()) {
                if (checkSymbol(")")) {
                    break;
                }

                if (check(TokenType.IDENTIFIER)) {
                    Token paramToken = advance(); // consume the identifier
                    params.add(paramToken.getText());
                } else {
                    error("Expected an identifier in parameter list", peek().getLine());
                }

                if (checkSymbol(",")) {
                    advance();
                } else if (checkSymbol(")")) {
                    break;
                } else {
                    error("Expected ',' or ')' in parameter list", peek().getLine());
                    break;
                }
            }

            if (!matchSymbol(")")) { // End of the function
                error("Missing ')' after parameter list", peek().getLine());
            }
        }

        return params;
    }

        /** Declaration of using global variables */
    private Statement parseGlobalStatement() {
        // Consume the 'GLOBAL' token
        Token globalToken = advance();

        List<String> vars = new ArrayList<>();

        while (!isAtEnd()) {
            if (match(TokenType.IDENTIFIER)) {
                Token varNameToken = previous();
                vars.add(varNameToken.getText());
            } else {
                if (checkSymbol(",")) {
                    advance(); // Just skip the comma
                    continue;
                } else {
                    break;
                }
            }
        }

        if (vars.isEmpty()) {
            error("Expected at least one identifier after 'global'", peek().getLine());
            return null;
        }

        return new GlobalStatement(vars);
    }

    private Statement parseFunctionCallStatement() {
        Token funcNameToken = advance(); // e.g. "puppetclear"
        String functionName = funcNameToken.getText();

        if (!matchSymbol("(")) {
            error("Expected '(' after function name in function call statement", peek().getLine());
            return null;
        }

        List<Expression> arguments = new ArrayList<>();

        while (!checkSymbol(")") && !isAtEnd()) {

            Expression argExpr = parseExpression();
            if (argExpr == null) {
                break;
            }
            arguments.add(argExpr);

            if (checkSymbol(",")) {
                advance();
            } else {
                break;
            }
        }

        if (!matchSymbol(")")) {
            error("Missing ')' after function call arguments", peek().getLine());
            // We can attempt to recover or just return null
            return null;
        }

        return new FunctionCallStatement(functionName, arguments);
    }

    /**
     * Used for function declaration
     */
    private List<String> parseParameterList() {
        List<String> params = new ArrayList<>();

        // If the next token is a ')' or EOF, no parameters
        while (!isAtEnd() && !checkSymbol(")")) {
            if (match(TokenType.IDENTIFIER)) {
                Token paramToken = previous();
                params.add(paramToken.getText());
            } else {
                error("Expected parameter identifier", peek().getLine());
                advance();
            }
        }

        return params;
    }

    /**
     * Used for function calls
     */
    private List<String> parseParameters() {
        List<String> params = new ArrayList<>();

        // Loop until we see a ')' or EOF
        while (!isAtEnd() && !checkSymbol(")")) {
            Token paramToken = peek();
            if (paramToken.getType() == TokenType.STRING
                    || paramToken.getType() == TokenType.NUMBER
                    || paramToken.getType() == TokenType.IDENTIFIER) {

                advance(); // Consume param
                params.add(paramToken.getText());
            } else {
                error("Expected parameter (string/number/identifier)", paramToken.getLine());
                advance();
            }

            if (checkSymbol(",")) {
                advance();
            } else {
                if (checkSymbol(")")) {
                    break;
                }
            }
        }

        return params;
    }

    /** @TODO incomplete as we do not handle multiple conditions (same thing applies for while conditions) */
    private Statement parseIfStatement() {
        advance();

        Condition condition = parseCondition();

        List<Statement> thenStmts = parseBlockStatements("else", "endif");

        List<Statement> elseStmts = new ArrayList<>();
        if (checkKeyword("else")) {
            advance();
            elseStmts = parseBlockStatements(null, "endif");
        }

        if (!matchKeyword("endif")) {
            error("Missing 'endif' after if/else block", peek().getLine());
            return null;
        }

        return new IfStatement(condition, thenStmts, elseStmts);
    }

    /**
     * Parses given statements list until one of the boundary keywords is encountered
     * like else or endif or just and EOF (But I have a doubt about processing like this).
     */
    private List<Statement> parseBlockStatements(String boundary1, String boundary2) {
        List<Statement> statements = new ArrayList<>();

        // Keep reading statements until we encounter one of the boundaries or EOF.
        while (!isAtEnd()) {
            Token tk = peek();

            if (tk.getType() == TokenType.KEYWORD) {
                String kw = tk.getText().toLowerCase();
                if (boundary1 != null && kw.equals(boundary1.toLowerCase())) {
                    break;
                }
                if (boundary2 != null && kw.equals(boundary2.toLowerCase())) {
                    break;
                }
            }

            Statement s = parseStatement();
            if (s != null) {
                statements.add(s);
            }
        }

        return statements;
    }

    /** @TODO buggy as we only handle only one condition */
    private Condition parseCondition() {
        Expression leftExpr = parseExpression();
        if (leftExpr == null) {
            error("Could not parse left side of if condition", peek().getLine());
            // Return a dummy condition to keep going
            return new Condition(leftExpr, "=", null);
        }

        Token eqToken = peek();
        if (eqToken.getType() == TokenType.SYMBOL && eqToken.getText().equals("=")) {
            advance();

            Expression rightExpr = parseExpression();
            if (rightExpr == null) {
                error("Could not parse right side of if condition", peek().getLine());
                return new Condition(leftExpr, "=", null);
            }

            return new Condition(leftExpr, "=", rightExpr);
        } else {
            error("Expected '=' in if condition", eqToken.getLine());

            return new Condition(leftExpr, "=", null);
        }
    }

    private Statement parseSwitchStatement() {
        advance();

        Expression switchExpr = parseExpression();
        if (switchExpr == null) {
            error("Could not parse expression after 'switch'", peek().getLine());
            return null;
        }

        List<CaseBlock> caseBlocks = new ArrayList<>();

        while (!isAtEnd()) {
            Token tk = peek();

            if (tk.getType() == TokenType.KEYWORD && tk.getText().equalsIgnoreCase("endswitch")) {
                break;
            }

            if (tk.getType() == TokenType.KEYWORD && tk.getText().equalsIgnoreCase("case")) {
                CaseBlock cb = parseCaseBlock();
                if (cb != null) {
                    caseBlocks.add(cb);
                }
            } else {
                error("Expected 'case' or 'endswitch' in switch block", tk.getLine());
                advance();
            }
        }

        if (!matchKeyword("endswitch")) {
            error("Missing 'endswitch' after switch block", peek().getLine());
            return null;
        }

        return new SwitchStatement(switchExpr, caseBlocks);
    }

    private Expression parseExpression() {

        Token tk = peek();
        if (tk.getType() == TokenType.IDENTIFIER) {
            Token next = lookAhead(1);
            if (next != null && next.getType() == TokenType.SYMBOL && next.getText().equals("(")) {
                return parseFunctionCallExpression();
            } else {
                advance();
                return new VariableExpression(tk.getText());
            }
        } else if (tk.getType() == TokenType.STRING
                || tk.getType() == TokenType.NUMBER
                || tk.getType() == TokenType.BOOLEAN) {
            advance();
            return new LiteralExpression(tk.getType(), tk.getText());
        }

        error("Unsupported expression format", tk.getLine());
        return null;
    }

    private Expression parseFunctionCallExpression() {
        // The current token is the function name
        Token funcNameToken = advance();
        String funcName = funcNameToken.getText();

        if (!matchSymbol("(")) {
            error("Expected '(' after function name in expression", peek().getLine());
            return null;
        }

        // Now the arguments
        List<Expression> args = new ArrayList<>();
        while (!isAtEnd()) {
            if (checkSymbol(")")) {
                break;
            }
            Expression argExpr = parseExpression();
            if (argExpr != null) {
                args.add(argExpr);
            }

            if (checkSymbol(",")) {
                advance();
            } else if (checkSymbol(")")) {
                break;
            } else {
                error("Expected ',' or ')' in function call arguments", peek().getLine());
                break;
            }
        }
        if (!matchSymbol(")")) {
            error("Missing ')' after function call arguments", peek().getLine());
            return null;
        }

        return new FunctionCallExpression(funcName, args);
    }

    private CaseBlock parseCaseBlock() {
        advance();

        Expression caseValueExpr = parseExpression();
        if (caseValueExpr == null) {
            error("Failed to parse case value expression", peek().getLine());
            return null;
        }

        List<Statement> stmts = new ArrayList<>();

        while (!isAtEnd()) {
            Token tk = peek();
            if (tk.getType() == TokenType.KEYWORD) {
                String kw = tk.getText().toLowerCase();
                if (kw.equals("case") || kw.equals("endswitch")) {
                    break;
                }
            }

            Statement s = parseStatement();
            if (s != null) {
                stmts.add(s);
            }
        }

        return new CaseBlock(caseValueExpr, stmts);
    }

    private boolean matchKeyword(String keyword) {
        if (checkKeyword(keyword)) {
            advance();
            return true;
        }
        return false;
    }

    private boolean checkKeyword(String keyword) {
        if (isAtEnd()) {
            return false;
        }
        Token t = peek();
        return (t.getType() == TokenType.KEYWORD && t.getText().equalsIgnoreCase(keyword));
    }

    private boolean isAtEnd() {
        return peek().getType() == TokenType.EOF;
    }

    /**
     * Returns the current token WITHOUT consuming it.
     */
    private Token peek() {
        if (current >= tokens.size()) {
            // If reaching the end of the file, we declare a EOF token.
            return new Token(TokenType.EOF, "", tokens.size());
        }
        return tokens.get(current);
    }

    /**
     * Consumes AND returns the current token, then advances the pointer.
     */
    private Token advance() {
        if (!isAtEnd()) {
            current++;
        }
        return previous();
    }

    /**
     * Returns true if the current token matches the given type, and advances if
     * it does.
     */
    private boolean match(TokenType type) {
        if (check(type)) {
            advance();
            return true;
        }
        return false;
    }

    /**
     * Returns true if current token is of the given type (without consuming
     * it).
     */
    private boolean check(TokenType type) {
        if (isAtEnd()) {
            return false;
        }
        return peek().getType() == type;
    }

    /**
     * Returns the previous consumed token.
     */
    private Token previous() {
        return tokens.get(current - 1);
    }

    private void error(String message, int line) {
        System.err.println("[Line " + line + "] Error: " + message);
    }

    private boolean matchSymbol(String symbol) {
        if (checkSymbol(symbol)) {
            advance();
            return true;
        }
        return false;
    }

    private boolean checkSymbol(String symbol) {
        if (isAtEnd()) {
            return false;
        }
        Token t = peek();
        return t.getType() == TokenType.SYMBOL && t.getText().equals(symbol);
    }

    private Token lookAhead(int offset) {
        int index = current + offset;
        if (index >= tokens.size()) {
            return null;
        }
        return tokens.get(index);
    }
}

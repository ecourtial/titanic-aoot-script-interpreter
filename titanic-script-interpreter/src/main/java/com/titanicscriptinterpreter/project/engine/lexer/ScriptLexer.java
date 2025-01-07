package com.titanicscriptinterpreter.project.engine.lexer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Open the file and read it line by line to produce a list of tokens.
 */
public class ScriptLexer {

    private final List<String> lines;
    private int currentLine = 0;

    public ScriptLexer(String scriptFilePath) throws IOException {
        /**
         * @TODO improve because we load the full file into memory
         */
        Path scriptPath = Path.of(scriptFilePath);
        this.lines = Files.readAllLines(scriptPath);
    }

    /**
     * Main method to tokenize the whole file. Returns a list of tokens from all
     * the lines of the script file.
     */
    public List<Token> lexAll() {
        List<Token> tokens = new ArrayList<>();
        while (currentLine < lines.size()) {
            String lineText = lines.get(currentLine);
            List<Token> currentTokens = tokenizeLine(lineText, currentLine + 1);
            tokens.addAll(currentTokens);
            
            // Dirty, but I did not find any other way to "stop" parsing the next line when not required
            if (
                !currentTokens.isEmpty()
                && (
                    currentTokens.get(0).getText().equals("global")
                    || currentTokens.get(0).getText().equals("local")
                )
            ) {
                tokens.add(new Token(TokenType.NEWLINE, "", currentLine + 1));
            }

            currentLine++;
        }
        
        // Add an EOF token
        tokens.add(new Token(TokenType.EOF, "", currentLine + 1));
        return tokens;
    }

    /**
     * Tokenize (split into tokens) a line of the script.
     */
    private List<Token> tokenizeLine(String lineText, int lineNumber) {
        List<Token> tokens = new ArrayList<>();

        // Ignore comments
        int commentIndex = lineText.indexOf("//");
        if (commentIndex != -1) {
            lineText = lineText.substring(0, commentIndex);
        }
        lineText = lineText.trim();
        if (lineText.isEmpty()) {
            return tokens;
        }

        // Trim leading/trailing spaces
        lineText = lineText.trim();
        if (lineText.isEmpty()) {
            return tokens; // no tokens on this line
        }

        // Scan manually. Could be improved with a regex?
        int i = 0;
        while (i < lineText.length()) {
            char c = lineText.charAt(i);

            // Skip whitespace
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }

            // Check for '(' or ')' or ',' => treat them as single SYMBOL tokens
            if (c == '(' || c == ')' || c == ',' || c == '=') {
                Token symToken = new Token(TokenType.SYMBOL, String.valueOf(c), lineNumber);
                tokens.add(symToken);
                i++;
                continue;
            }

            // Check for double-quoted string
            if (c == '"') {
                // Read until the next quote
                int startIndex = i;
                i++; // skip the opening quote

                StringBuilder sb = new StringBuilder();
                while (i < lineText.length() && lineText.charAt(i) != '"') {
                    sb.append(lineText.charAt(i));
                    i++;
                }

                // Should now be at the closing quote, or end of line
                if (i >= lineText.length()) {
                    // We didn't find a closing quote -> error or treat as ended
                    System.err.println("[Lexer] Unterminated string at line " + lineNumber);
                    // We can still add what we have as a STRING token if we want
                }
                i++; // skip the closing quote, if present

                String stringContent = sb.toString();
                Token strToken = new Token(TokenType.STRING, stringContent, lineNumber);
                tokens.add(strToken);

                continue;
            }

            // Otherwise, it might be a number, identifier, or keyword.
            // In this case onsume characters until we hit whitespace, parens, comma, or end-of-line.
            int startIndex = i;
            while (i < lineText.length()) {
                char ch = lineText.charAt(i);
                if (Character.isWhitespace(ch) || ch == '(' || ch == ')' || ch == ',' || ch == '"') {
                    break;
                }
                i++;
            }

            String chunk = lineText.substring(startIndex, i);

            // Classify this chunk as a token.
            tokens.add(classifyToken(chunk, lineNumber));
        }

        return tokens;
    }

    /**
     * Try to assess if a given string is a token
     */
    private Token classifyToken(String text, int lineNumber) {
        if (text.equalsIgnoreCase("true") || text.equalsIgnoreCase("false")) {
            return new Token(TokenType.BOOLEAN, text.toLowerCase(), lineNumber);
        }

        if (text.equals("(") || text.equals(")")) {
            return new Token(TokenType.SYMBOL, text, lineNumber);
        }


        if (isKeyword(text)) {
            return new Token(TokenType.KEYWORD, text, lineNumber);
        }

        if (text.matches("\\d+")) {
            return new Token(TokenType.NUMBER, text, lineNumber);
        }

        if (text.startsWith("\"") && text.endsWith("\"") && text.length() >= 2) {
            String content = text.substring(1, text.length() - 1);
            return new Token(TokenType.STRING, content, lineNumber);
        }

        if (text.equalsIgnoreCase("switch"))  return new Token(TokenType.KEYWORD, "switch", lineNumber);
        if (text.equalsIgnoreCase("case"))    return new Token(TokenType.KEYWORD, "case",   lineNumber);
        if (text.equalsIgnoreCase("endswitch")) return new Token(TokenType.KEYWORD, "endswitch", lineNumber);


        // Fallback
        return new Token(TokenType.IDENTIFIER, text, lineNumber);
    }

    private boolean isKeyword(String text) {
        switch (text) {
            case "code": // start of a function
            case "endcode": // end of a function
            case "global": // declare the use of global variables
            case "local": // create local variables
            case "if":
            case "else":
            case "endif":
            case "exitcode": // script termination
            case "switch":
            case "case":
            case "endswitch":
            case "while":
            case "endwhile":
            case "return":
                return true;
            default:
                return false;
        }
    }
}

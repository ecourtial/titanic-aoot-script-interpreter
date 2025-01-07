package com.titanicscriptinterpreter.project.engine.lexer;

public enum TokenType {
    KEYWORD,     // if, endif...
    IDENTIFIER,  // Variable names or labels
    STRING,
    NUMBER,
    SYMBOL,      // Punctuation, parentheses, etc.
    EOF,         // End of file (can be manually triggered)
    UNKNOWN,      // Fallback if we can’t classify a token, but will trigger an error.
    BOOLEAN,
    NEWLINE
}

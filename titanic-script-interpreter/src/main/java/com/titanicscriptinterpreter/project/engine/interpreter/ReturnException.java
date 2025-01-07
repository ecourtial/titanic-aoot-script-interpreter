package com.titanicscriptinterpreter.project.engine.interpreter;

/**
 * ReturnException:
 * Thrown when a 'return' statement is encountered in the script,
 * carrying the return value. The interpreter catches this exception
 * where the function call was made, so it can resume normal flow
 * with the given return value.
 */
public class ReturnException extends RuntimeException {
    private final Object returnValue;

    public ReturnException(Object returnValue) {
        this.returnValue = returnValue;
    }

    public Object getReturnValue() {
        return returnValue;
    }
}

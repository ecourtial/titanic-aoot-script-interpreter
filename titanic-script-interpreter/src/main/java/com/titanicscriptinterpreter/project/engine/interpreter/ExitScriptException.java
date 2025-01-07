package com.titanicscriptinterpreter.project.engine.interpreter;

public class ExitScriptException extends RuntimeException {
    public ExitScriptException() {
        super("Script execution was terminated by 'exitcode'");
    }
}

package com.titanicscriptinterpreter.project.engine;

public class Cli {
    public static void outputMessage(String message) {
        System.out.println(message);
    }
    
    public static void outputTitle(String message) {
        Cli.outputNewLine();
        System.out.println("=== " + message + " ===");
        Cli.outputNewLine();
    }
    
    public static void outputNewLine() {
        System.out.println("");
    }
}

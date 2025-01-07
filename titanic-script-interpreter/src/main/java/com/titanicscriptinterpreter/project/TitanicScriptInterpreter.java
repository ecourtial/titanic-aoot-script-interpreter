package com.titanicscriptinterpreter.project;

import com.titanicscriptinterpreter.project.engine.Cli;
import com.titanicscriptinterpreter.project.engine.interpreter.ExitScriptException;
import com.titanicscriptinterpreter.project.engine.interpreter.Interpreter;
import com.titanicscriptinterpreter.project.engine.lexer.Parser;
import com.titanicscriptinterpreter.project.engine.lexer.ScriptLexer;
import com.titanicscriptinterpreter.project.engine.statements.Statement;
import com.titanicscriptinterpreter.project.engine.lexer.Token;
import java.io.IOException;
import java.util.List;
import com.titanicscriptinterpreter.project.engine.interpreter.ScriptContext;
import java.util.ArrayList;
import java.util.Scanner;

public class TitanicScriptInterpreter {

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("Usage: java -jar my-interpreter.jar <scriptFilePath>");
            System.exit(1);
        }

        String scriptPath = args[0];

        // Lex the script to extract tokens
        Cli.outputTitle("LEXER...");
        ScriptLexer lexer = new ScriptLexer(scriptPath);
        List<Token> tokens = lexer.lexAll();
        Cli.outputMessage("Tokens extracted.");

        // Parse the tokens to create statements
        Cli.outputTitle("PARSING THE SCRIPT TO CREATE STATEMENTS");
        Parser parser = new Parser(tokens);
        List<Statement> statements = parser.parse();

        for (Statement stmt : statements) {
            System.out.println(stmt);
        }

        // Create the script context with the proper global variables for the game status
        ScriptContext gameContext = new ScriptContext();
        gameContext.setVariable("mission", 1);
        gameContext.setVariable("phase", 0);
        gameContext.setVariable("pennybrush", "firstEncounter");

        // Launch the interpreter to register functions
        Cli.outputTitle("LAUNCHING THE INTERPRETER...");
        Interpreter interpreter = new Interpreter(gameContext);
        interpreter.interpretStatements(statements, gameContext);

        // Launch the engine by calling the first function
        List<Object> argValues = new ArrayList<>();
        Cli.outputTitle("BEGINNING OF THE GAME");

        try (Scanner scanner = new Scanner(System.in)) {
            String input;
            String command;

            // Loop until the user enters "exit". On the first time, we should enter "runyoself"
            while (true) {
                Cli.outputMessage("Enter a function to execute or enter 'exit': ");
                input = scanner.nextLine();

                if (input.equalsIgnoreCase("exit")) {
                    Cli.outputTitle("EXITING...");
                    break; // Exit the loop
                }

                // Split the input into words
                String[] parts = input.split("\\s+");

                // Assign the first word to command and the rest to argValues
                command = parts[0];
                argValues.clear();  // Clear the previous arguments
                for (int i = 1; i < parts.length; i++) {
                    argValues.add(parts[i]);
                }

                // System.out.println("Command: " + command);
                // System.out.println("Arguments: " + argValues);
                try {
                    Cli.outputNewLine();
                    interpreter.callFunction(command, argValues, gameContext);
                } catch (ExitScriptException e) {
                    Cli.outputNewLine();
                    Cli.outputMessage("Function ended");
                }
            }
        }
    }
}
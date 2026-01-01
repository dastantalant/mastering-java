package com.mastering.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Lox {
    private static boolean hadError;
    private static boolean hadRuntimeError;
    private static final Interpreter interpreter = new Interpreter();

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runScript(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runScript(String path) throws IOException {
        String source = Files.readString(Path.of(path));
        run(source);

        if (hadError) {
            System.exit(65);
        }

        if (hadRuntimeError) {
            System.exit(70);
        }
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (; ; ) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            run(line);
            hadError = false;
        }
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        for (Token token : tokens) {
            System.out.println("token = " + token);
        }

        Parser parser = new Parser(tokens);
        Expr expression = parser.parse();

        if (hadError) {
            return;
        }

        System.out.println(new AstPrinter().print(expression));

        interpreter.interpret(expression);
    }

    static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error " + where + ": " + message);
        hadError = true;
    }

    public static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    public static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() + "\n[line " + error.token.line + "]");
        hadRuntimeError = true;
    }
}
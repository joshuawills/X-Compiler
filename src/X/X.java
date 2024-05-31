package X;

import X.Checker.Checker;
import X.Lexer.File;
import X.Lexer.Lex;
import X.Lexer.Token;
import X.Nodes.AST;
import X.Parser.Parser;
import X.Parser.SyntaxError;
import X.TreeDrawer.Drawer;

import java.util.ArrayList;
import java.util.HashMap;

public class X {

    private static AST ast;

    private static void help() {
        System.out.println("XY Compiler Options:");
        System.out.println("\t-h | --help => Provides summary of CL arguments and use of program");         // HANDLED
        System.out.println("\t-r | --run => Will run the program after compilation");
        System.out.println("\t-o | --out => Specify the name of the executable (default to a.out)");
        System.out.println("\t-t | --tokens => Logs to stdout a summary of all the tokens");                // HANDLED
        System.out.println("\t-p | --parser => Generates a graphic parse tree");                            // HANDLED
        System.out.println("\t-a | --assembly => Generates a .s file instead of an executable");
        System.out.println("\t-q | --quiet  => Silence any non-crucial warnings");
        System.out.println("\nDeveloped by Joshua Wills 2024");
        System.exit(0);
    }

    private static HashMap<String, String> handleArgs(String[] args) {
        HashMap<String, String> CLArgs = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-h", "--help" -> {
                    CLArgs.put("help", "true");
                }
                case "-r", "--run" -> {
                    CLArgs.put("run", "true");
                }
                case "-o", "--out" -> {
                    if (i + 1 == args.length)
                        break;
                    CLArgs.put("exe", args[i + 1]);
                    i += 1;
                }
                case "-t", "--tokens" -> {
                    CLArgs.put("tokens", "true");
                }
                case "-p", "--parser" -> {
                    CLArgs.put("parse", "true");
                }
                case "-a", "--assembly" -> {
                    CLArgs.put("asm", "true");
                }
                case "-q", "--quiet" -> {
                    CLArgs.put("quiet", "true");
                }
                default -> {
                    // Assume source filename is provided
                    CLArgs.put("source", args[i]);
                }
            }
        }
        return CLArgs;
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: ./executable <options> <filename.x>");
            X.help();
            System.exit(1);
        }

        String file_name = args[0];

        HashMap<String, String> clARGS = X.handleArgs(args);
        if (clARGS.containsKey("help")) {
            X.help();
        }

        ErrorHandler handler = new ErrorHandler();
        File file = new File(file_name);

        Lex lexer = new Lex(file);
        ArrayList<Token> tokens = lexer.getTokens();

        if (clARGS.containsKey("tokens")) {
            for (Token token: tokens) {
                System.out.println(token);
            }
            System.exit(0);
        }

        Parser parser = new Parser(tokens, handler);

        try {
            ast = parser.parseProgram();
        } catch (SyntaxError s) {
            System.exit(1);
        }

        if (handler.numErrors == 0 && clARGS.containsKey("parse")) {
            Drawer drawer = new Drawer();
            drawer.draw(ast);
            System.exit(0);
        }

        Checker checker = new Checker(handler);
        try {
            checker.check(ast);
        } catch (Exception s) {
            System.exit(1);
        }
    }

}
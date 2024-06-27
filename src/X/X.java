package X;

import X.Checker.Checker;
import X.CodeGen.Emitter;
import X.Lexer.File;
import X.Lexer.Lex;
import X.Lexer.Token;
import X.Nodes.AST;
import X.Parser.Parser;
import X.Parser.SyntaxError;
import X.TreeDrawer.Drawer;
import X.TreePrinter.Printer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;

public class X {

    private static AST ast;
    private static final DecimalFormat df = new DecimalFormat("0.00");
    private static void help() {
        System.out.println("XY Compiler Options:");
        System.out.println("\t-h  | --help => Provides summary of CL arguments and use of program");
        System.out.println("\t-r  | --run => Will run the program after compilation");
        System.out.println("\t-o  | --out => Specify the name of the executable (default to a.out)");
        System.out.println("\t-t  | --tokens => Logs to stdout a summary of all the tokens");
        System.out.println("\t-p  | --parser => Generates a graphic parse tree");
        System.out.println("\t-pr | --parser_raw => Generates a deconstructed parse tree");
        System.out.println("\t-a  | --assembly => Generates a .s file instead of an executable");
        System.out.println("\t-q  | --quiet  => Silence any non-crucial warnings");
        System.out.println("\t-s | --stat => Log statistics about the compilation times");
        System.out.println("\nDeveloped by Joshua Wills 2024");
        System.exit(0);
    }

    private static HashMap<String, String> handleArgs(String[] args) {
        HashMap<String, String> CLArgs = new HashMap<>();
        for (int i = 0; i < args.length; i += 1) {
            switch (args[i]) {
                case "-h", "--help" -> CLArgs.put("help", "true");
                case "-pr", "--parser_raw" -> CLArgs.put("parser_raw", "true");
                case "-r", "--run" -> CLArgs.put("run", "true");
                case "-t", "--tokens" -> CLArgs.put("tokens", "true");
                case "-p", "--parser" -> CLArgs.put("parse", "true");
                case "-a", "--assembly" -> CLArgs.put("asm", "true");
                case "-q", "--quiet" -> CLArgs.put("quiet", "true");
                case "-s", "--stat" -> CLArgs.put("stat", "true");
                case "-o", "--out" -> {
                    if (i + 1 == args.length)
                        break;
                    CLArgs.put("exe", args[i + 1]);
                    i += 1;
                }
                default -> CLArgs.put("source", args[i]);
            }
        }
        return CLArgs;
    }

    private static void shellCommand(String command, boolean printOutput) {
        try {
            Process process = Runtime.getRuntime().exec(command);

            if (printOutput) {
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
                BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

                String s;
                while ((s = stdInput.readLine()) != null) {
                    System.out.println(s);
                }
                while ((s = stdError.readLine()) != null) {
                    System.err.println(s);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.out.println("Failed to execute shell command with code 0: " + command);
            }
        } catch (Exception e) {
            System.out.println("'" + command + "' command failed. Is this program installed on your system?");
        }
    }
    public static void main(String[] args) {
        Instant start = Instant.now();
        if (args.length == 0) {
            System.out.println("Usage: ./executable <options> <filename.x>");
            X.help();
            System.exit(1);
        }

        HashMap<String, String> clARGS = X.handleArgs(args);
        if (clARGS.containsKey("help")) {
            X.help();
        }

        String file_name = clARGS.get("source");

        ErrorHandler handler = new ErrorHandler(file_name, clARGS.containsKey("quiet"));
        File file = new File(file_name);

        Instant lStart = Instant.now();
        Lex lexer = new Lex(file);
        ArrayList<Token> tokens = lexer.getTokens();
        Instant lEnd = Instant.now();

        if (clARGS.containsKey("tokens")) {
            for (Token token: tokens) {
                System.out.println(token);
            }
            System.exit(0);
        }

        Instant pStart = Instant.now();
        Parser parser = new Parser(tokens, handler);
        try {
            ast = parser.parseProgram();
        } catch (SyntaxError s) {
            System.exit(1);
        }
        Instant pEnd = Instant.now();

        if (handler.numErrors == 0 && clARGS.containsKey("parser_raw")) {
            Printer printer = new Printer("tests/.tree");
            printer.print(ast);
            System.exit(0);
        }

        if (handler.numErrors == 0 && clARGS.containsKey("parse")) {
            Drawer drawer = new Drawer();
            drawer.draw(ast);
        } else {
            Instant cStart = Instant.now();
            Checker checker = new Checker(handler);
            try {
                checker.check(ast);
            } catch (Exception s) {
                System.out.println(s);
                System.exit(1);
            }
            Instant cEnd = Instant.now();

            if (handler.numErrors != 0) {
                System.exit(1);
            }

            String file_name_format = "out.ll";
            if (clARGS.containsKey("asm")) {
                file_name_format = clARGS.getOrDefault("exe", "out.ll");
            }

            Instant eStart = Instant.now();
            if (handler.numErrors == 0) {
                if (!file_name_format.endsWith(".ll")) {
                    System.out.println("IR file must end with '.ll' file suffix");
                }
                Emitter emitter = new Emitter(file_name_format);
                emitter.gen(ast);
            }
            Instant eEnd = Instant.now();

            Instant clangStart = Instant.now();
            if (!clARGS.containsKey("asm")) {
                shellCommand("clang -o " + clARGS.getOrDefault("exe", "a.out") + " " + file_name_format, false);
                shellCommand("rm -f " + file_name_format, false);
            }
            Instant clangEnd = Instant.now();

            Instant runStart = Instant.now();
            if (clARGS.containsKey("run")) {
                shellCommand("./" + clARGS.getOrDefault("exe", "a.out"), true);
            }
            Instant runEnd = Instant.now();

            if (clARGS.containsKey("stat")) {
                Instant end = Instant.now();
                System.out.println("Compiler data");
                long timeMS = Duration.between(start, end).toMillis();
                long timeS= Duration.between(start, end).toSeconds();
                System.out.println("\tTotal time elapsed: " + timeMS + "ms/" + df.format(timeS) + "s\n");
                long lTimeMS = Duration.between(lStart, lEnd).toMillis();
                long lTimeS= Duration.between(lStart, lEnd).toSeconds();
                System.out.println("\tLexing time:        " + lTimeMS + "ms/" + df.format(lTimeS) + "s " + (100 * lTimeMS / timeMS) + "%");
                long pTimeMS = Duration.between(pStart, pEnd).toMillis();
                long pTimeS= Duration.between(pStart, pEnd).toSeconds();
                System.out.println("\tParsing time:       " + pTimeMS + "ms/" + df.format(pTimeS) + "s " + (100 * pTimeMS / timeMS) + "%");
                long cTimeMS = Duration.between(cStart, cEnd).toMillis();
                long cTimeS= Duration.between(cStart, cEnd).toSeconds();
                System.out.println("\tChecking time:      " + cTimeMS + "ms/" + df.format(cTimeS) + "s " + (100 * cTimeMS / timeMS) + "%");
                long eTimeMS = Duration.between(eStart, eEnd).toMillis();
                long eTimeS= Duration.between(eStart, eEnd).toSeconds();
                System.out.println("\tEmitting time:      " + eTimeMS + "ms/" + df.format(eTimeS) + "s " + (100 * eTimeMS / timeMS) + "%");
                if (!clARGS.containsKey("asm")) {
                    long clangTimeMS = Duration.between(clangStart, clangEnd).toMillis();
                    long clangTimeS= Duration.between(clangStart, clangEnd).toSeconds();
                    System.out.println("\tClang time:         " + clangTimeMS + "ms/" + df.format(clangTimeS) + "s " + (100 * clangTimeMS / timeMS) + "%");
                }
                if (clARGS.containsKey("run")) {
                    long runTimeMS = Duration.between(runStart, runEnd).toMillis();
                    long runTimeS= Duration.between(runStart, runEnd).toSeconds();
                    System.out.println("\tRun time:           " + runTimeMS + "ms/" + df.format(runTimeS) + "s " + (100 * runTimeMS / timeMS) + "%");
                }
            }
        }
    }
}
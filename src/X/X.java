package X;

import X.Lexer.File;
import X.Lexer.Lex;
import X.Lexer.Token;
import X.Nodes.AST;
import X.Parser.Parser;
import X.Parser.SyntaxError;

import java.util.ArrayList;

public class X {

    private static AST ast;

    public static void main(String[] args) {

        System.out.println("The 'X' Compiler");

        if (args.length == 0) {
            System.err.println("Please provide a filename");
            System.exit(1);
        }

        String file_name = args[0];

        ErrorHandler handler = new ErrorHandler();
        File file = new File(file_name);

        Lex lexer = new Lex(file);
        ArrayList<Token> tokens = lexer.getTokens();

//        for (Token x : tokens) {
//            System.out.println(x);
//        }

        Parser parser = new Parser(tokens, handler);

        try {
            ast = parser.parseProgram();
        } catch (SyntaxError s) {
            System.exit(1);
        }
    }

}
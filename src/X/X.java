package X;

import java.util.ArrayList;

import X.Lexer.File;
import X.Lexer.Lex;
import X.Lexer.Token;

public class X {

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

        // Logs tokens
        for (Token x: tokens) {
            System.out.println(x);
        }

    }

}
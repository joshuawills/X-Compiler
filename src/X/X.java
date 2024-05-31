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

public class X {

    private static AST ast;

    public static void main(String[] args) {

        if (args.length == 0) {
            System.err.println("Please provide a filename");
            System.exit(1);
        }

        String file_name = args[0];

        ErrorHandler handler = new ErrorHandler();
        File file = new File(file_name);

        Lex lexer = new Lex(file);
        ArrayList<Token> tokens = lexer.getTokens();

        Parser parser = new Parser(tokens, handler);

        try {
            ast = parser.parseProgram();
        } catch (SyntaxError s) {
            System.exit(1);
        }

        if (handler.numErrors == 0) {
            // Drawer drawer = new Drawer();
            // drawer.draw(ast);
        }


        Checker checker = new Checker(handler);
        try {
            checker.check(ast);
        } catch (Exception s) {
            System.exit(1);
        }
    }

}
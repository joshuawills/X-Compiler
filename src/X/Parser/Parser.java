package X.Parser;

import java.util.ArrayList;

import X.ErrorHandler;
import X.Lexer.Token;

public class Parser {
    
    private final ArrayList<Token> tokenStream;
    private final ErrorHandler handler;

    public Parser(ArrayList<Token> tokenStream, ErrorHandler handler) {
        this.tokenStream = tokenStream;
        this.handler = handler;
    }

}

package X;

import X.Lexer.Position;

public class ErrorHandler {
    
    public int numErrors = 0;

    public void reportError(String message, String tokenName, Position pos) {
        System.out.println("ERROR: ");
        System.out.println(pos);
        for (int c = 0; c < message.length(); c++) {
            if (message.charAt(c) == '%') {
                System.out.print(tokenName);
            } else {
                System.out.print(message.charAt(c));
            }
        }
        System.out.println();
        numErrors++;
    }

}

package X.Lexer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.LineNumberReader;

public class File {
    
    public static char EOF = '\u0000';
    private LineNumberReader fileReader;

    public File(String filename) {
        try {
            fileReader = new LineNumberReader(new BufferedReader(new FileReader(filename)));
        } catch (Exception e) {
            System.err.println("Unable to read file: " + e);
            System.exit(1);
        }
    }

    public char getNextChar() {
        int c = -1;
        try {
            c = fileReader.read();
        } catch (Exception e) {
            System.err.println("Unable to read file: " + e);
            System.exit(1);
        }
        if (c == -1) {
            c = EOF;
        }
        return (char) c;
    }

    public char inspectChar(int nthChar) {
        int c = -1;
        try {
            fileReader.mark(nthChar);

            do {
                c = fileReader.read();
                nthChar -= 1;
            } while (nthChar != 0);

            fileReader.reset();

            if (c == -1) {
                c = EOF;
            }
        } catch (Exception e) {
            System.err.println("Unable to read file: " + e);
            System.exit(1);
        }

        return (char) c;
    }

}

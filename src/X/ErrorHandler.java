package X;

import X.Lexer.Position;

import java.nio.file.Files;
import java.nio.file.Path;

public class ErrorHandler {

    public boolean isQuiet;
    public int numErrors = 0;
    public String fileName;
    public int numLines;
    public String fileContents;

    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BLUE = "\u001b[34m";
    private static final String ANSI_YELLOW = "\u001B[33m";

    public boolean isQuiet() {
        return isQuiet;
    }

    public ErrorHandler(String fileName, boolean isQuiet) {
        this.isQuiet = isQuiet;
        this.fileName = fileName;
        try {
            fileContents = Files.readString(Path.of(fileName));
        } catch (Exception e) {
            System.out.println("Unable to read file: " + fileName);
            System.exit(1);
        }
        numLines = fileContents.split("\n").length;
    }

    public void reportError(String message, String tokenName, Position pos) {
        System.out.print(ANSI_RED + "ERROR: " + ANSI_RESET);
        for (int c = 0; c < message.length(); c++) {
            if (message.charAt(c) == '%') {
                System.out.print(tokenName);
            } else {
                System.out.print(message.charAt(c));
            }
        }
        System.out.println();
        logLines(pos.lineStart, pos.charStart);
        numErrors++;
    }

    public void reportMinorError(String message, String tokenName, Position pos) {
        if (isQuiet) return;
        System.out.print(ANSI_BLUE + "MINOR ERROR: " + ANSI_RESET);
        for (int c = 0; c < message.length(); c++) {
            if (message.charAt(c) == '%') {
                System.out.print(tokenName);
            } else {
                System.out.print(message.charAt(c));
            }
        }
        System.out.println();
        logLines(pos.lineStart, pos.charStart);
    }

    public void logLines(int line, int col) {
        System.out.println(ANSI_YELLOW + this.fileName + ":" + line + ":" + col + ANSI_RESET + ":");
        for (int i = line - 2; i <= line + 2; i++) {
            if (i >= 1 && i <= this.numLines) {
                System.out.printf("%5s | ", i);
                System.out.println(this.fileContents.split("\n")[i - 1]);
            }
        }
        System.out.println();
    }
}

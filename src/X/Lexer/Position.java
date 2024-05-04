package X.Lexer;

public class Position {

    public int lineStart, lineFinish, charStart, charFinish;

    public Position() {
        lineStart = lineFinish = charStart = charFinish = 0;
    }

    public Position(int lineStart, int lineFinish, int charStart, int charFinish) {
        this.lineStart = lineStart;
        this.lineFinish = lineFinish;
        this.charStart = charStart;
        this.charFinish = charFinish;
    }

    @Override
    public String toString() {
        return String.format("%d(%d)..%d(%d)", lineStart, charStart, lineFinish, charFinish);
    }

}

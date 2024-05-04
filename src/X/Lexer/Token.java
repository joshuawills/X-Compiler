package X.Lexer;

public class Token {

    public TokenType kind;
    public String lexeme;
    public Position pos;

    public Token(TokenType kind, String lexeme, Position pos) {
        this.kind = kind;
        this.lexeme = lexeme;
        this.pos = pos;
    }

    @Override
    public String toString() {
        return String.format("Kind = %s [%s], Position = %s", kind, lexeme, pos);
    }

}

package X.Nodes;

import X.Lexer.Position;

public abstract class Terminal extends AST {

    public String spelling;

    public Terminal(String spelling, Position pos) {
        super(pos);
        this.spelling = spelling;
    }

}

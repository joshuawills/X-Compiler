package X.Nodes;

import X.Lexer.Position;

public abstract class Expr extends AST {

    public Type type;

    public Expr(Position pos) {
        super(pos);
        type = null;
    }

}

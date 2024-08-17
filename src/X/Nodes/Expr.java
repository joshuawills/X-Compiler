package X.Nodes;

import X.Lexer.Position;

public abstract class Expr extends AST {

    public Type type;
    public int tempIndex;
    public boolean isLHSOfAssignment;

    public Expr(Position pos) {
        super(pos);
        type = null;
    }

}

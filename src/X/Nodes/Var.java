package X.Nodes;

import X.Lexer.Position;

public abstract class Var extends AST {

    public Type type;

    public Var(Position pos) {
        super(pos);
        type = null;
    }

}

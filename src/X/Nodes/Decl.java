package X.Nodes;

import X.Lexer.Position;

public abstract class Decl extends AST {

    public Type T;
    public Ident I;

    public int index;

    public boolean isMut;

    public Decl(Position pos, boolean isMut) {
        super(pos);
        this.isMut = isMut;
    }

}

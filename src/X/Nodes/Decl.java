package X.Nodes;

import X.Lexer.Position;

public abstract class Decl extends AST {

    public Type T;
    public Ident I;

    public String index;
    public boolean isUsed = false;
    public boolean isReassigned = false;

    public boolean isMut;
    public boolean isExported = false;

    public Decl(Position pos, boolean isMut) {
        super(pos);
        this.isMut = isMut;
    }

    public boolean isFunction() {
        return this instanceof Function;
    }

    public void setExported() {
        isExported = true;
    }

}

package X.Nodes;

import X.Lexer.Position;

public class Extern extends Decl {

    public Function F = null;
    public GlobalVar G = null;

    public Extern(Function fAST, Position pos) {
        super(pos, false);
        F = fAST;
        F.parent = this;
    }

    public Extern(GlobalVar gAST, Position pos) {
        super(pos, false);
        G = gAST;
        G.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitExtern(this, o);
    }
    
}
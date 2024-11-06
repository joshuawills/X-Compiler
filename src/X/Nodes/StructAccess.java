package X.Nodes;

import X.Lexer.Position;

public class StructAccess extends Expr {

    public Struct ref; // this is the reference struct
    public Ident varName;
    public StructAccessList L;

    public StructAccess(Struct refAST, Ident varNameAST, StructAccessList lAST, Position pos) {
        super(pos);
        ref = refAST;
        varName = varNameAST;
        L = lAST;
        ref.parent = varName.parent = L.parent = this;
    }

    @Override
    public Object visit(Visitor v, Object o) {
        return v.visitStructAccess(this, o);
    }
}

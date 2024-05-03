package X.Nodes;

import X.Lexer.Position;

public class GlobalVar extends Decl {

    public Expr E; // should only be assign or empty

    public GlobalVar(Type tAST, Ident iAST, Expr eAST, Position pos) {
        super(pos);
        T = tAST;
        I = iAST;
        E = eAST;
        T.parent = I.parent = E.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitGlobalVar(this, o);
    }
}

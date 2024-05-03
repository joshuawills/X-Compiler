package X.Nodes;

import X.Lexer.Position;

public class LocalVar extends Decl {
    public Expr E; // should only be assign or empty
    public LocalVar(Type tAST, Ident iAST, Expr eAST, Position pos) {
        super(pos);
        T = tAST;
        I = iAST;
        E = eAST;
        T.parent = I.parent = E.parent = this;
    }
    public Object visit(Visitor v, Object o) {
        return v.visitLocalVar(this, o);
    }
}
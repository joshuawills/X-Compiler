package X.Nodes;

import X.Lexer.Position;

public class DotExpr extends Expr {

    public Ident I;
    public Expr E;

    public DotExpr(Ident iAST, Expr eAST, Position pos) {
        super(pos);
        I = iAST;
        E = eAST;
        I.parent = E.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitDotExpr(this, o);
    }

}
package X.Nodes;

import X.Lexer.Position;

public class DerefExpr extends Expr {

    public Expr E;

    public DerefExpr(Expr eAST, Position pos) {
        super(pos);
        E = eAST;
        E.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitDerefExpr(this, o);
    }
}

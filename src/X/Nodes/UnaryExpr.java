package X.Nodes;

import X.Lexer.Position;

public class UnaryExpr extends Expr {

    public Operator O;
    public Expr E;

    public UnaryExpr(Operator oAST, Expr eAST, Position pos) {
        super(pos);
        O = oAST;
        E = eAST;
        O.parent = E.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitUnaryExpr(this, o);
    }

}

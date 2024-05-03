package X.Nodes;

import X.Lexer.Position;

public class EmptyExpr extends Expr {

    public EmptyExpr(Position pos) {
        super(pos);
    }

    public Object visit(Visitor v, Object o) {
        return v.visitEmptyExpr(this, o);
    }
}

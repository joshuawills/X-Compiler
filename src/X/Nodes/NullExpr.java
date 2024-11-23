package X.Nodes;

import X.Lexer.Position;

public class NullExpr extends Expr {

    public NullExpr(Position pos) {
        super(pos);
    }

    public Object visit(Visitor v, Object o) {
        return v.visitNullExpr(this, o);
    }
    
}

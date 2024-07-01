package X.Nodes;

import X.Lexer.Position;

public class FloatExpr extends Expr {

    public FloatLiteral FL;

    public FloatExpr(FloatLiteral flAST, Position pos) {
        super(pos);
        FL = flAST;
        FL.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitFloatExpr(this, o);
    }
}

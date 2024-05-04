package X.Nodes;

import X.Lexer.Position;

public class BooleanExpr extends Expr {

    public BooleanLiteral BL;

    public BooleanExpr(BooleanLiteral blAST, Position pos) {
        super(pos);
        BL = blAST;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitBooleanExpr(this, o);
    }

}

package X.Nodes;

import X.Lexer.Position;

public class CharExpr extends Expr {

    public CharLiteral CL;

    public CharExpr(CharLiteral clAST, Position pos) {
        super(pos);
        CL = clAST;
        CL.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitCharExpr(this, o);
    }
}

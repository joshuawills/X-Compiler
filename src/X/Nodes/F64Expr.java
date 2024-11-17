package X.Nodes;

import X.Lexer.Position;

public class F64Expr extends Expr {

    public DecimalLiteral DL;

    public F64Expr(DecimalLiteral flAST, Position pos) {
        super(pos);
        DL = flAST;
        DL.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitF64Expr(this, o);
    }

}
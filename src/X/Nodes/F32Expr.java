package X.Nodes;

import X.Lexer.Position;

public class F32Expr extends Expr {

    public DecimalLiteral DL;

    public F32Expr(DecimalLiteral flAST, Position pos) {
        super(pos);
        DL = flAST;
        DL.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitF32Expr(this, o);
    }
}

package X.Nodes;

import X.Lexer.Position;

public class StringExpr extends Expr {

    public StringLiteral SL;
    public int Identifier;

    public StringExpr(StringLiteral slAST, Position pos) {
        super(pos);
        SL = slAST;
        SL.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitStringExpr(this, o);
    }
}

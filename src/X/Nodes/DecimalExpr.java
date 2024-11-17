package X.Nodes;

import X.Lexer.Position;

public class DecimalExpr extends Expr {

    public DecimalLiteral DL;

    public DecimalExpr(DecimalLiteral dlAST, Position pos) {
        super(pos);
        DL = dlAST;
        DL.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitDecimalExpr(this, o);
    }
    
}

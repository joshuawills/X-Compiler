package X.Nodes;

import X.Lexer.Position;

public class IntExpr extends Expr {

    public IntLiteral IL;

    public IntExpr(IntLiteral ilAST, Position pos) {
        super(pos);
        IL = ilAST;
        IL.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitIntExpr(this, o);
    }
}

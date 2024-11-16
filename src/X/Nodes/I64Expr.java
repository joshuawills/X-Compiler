package X.Nodes;

import X.Lexer.Position;

public class I64Expr extends Expr {

    public IntLiteral IL;

    public I64Expr(IntLiteral ilAST, Position pos) {
        super(pos);
        IL = ilAST;
        IL.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitI64Expr(this, o);
    }
}

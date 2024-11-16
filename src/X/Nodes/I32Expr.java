package X.Nodes;

import X.Lexer.Position;

public class I32Expr extends Expr {

    public IntLiteral IL;

    public I32Expr(IntLiteral ilAST, Position pos) {
        super(pos);
        IL = ilAST;
        IL.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitI32Expr(this, o);
    }
    
}

package X.Nodes;

import X.Lexer.Position;

public class U8Expr extends Expr {

    public IntLiteral IL;

    public U8Expr(IntLiteral ilAST, Position pos) {
        super(pos);
        IL = ilAST;
        IL.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitU8Expr(this, o);
    }
    
}

package X.Nodes;

import X.Lexer.Position;

public class U32Expr extends Expr {

    public IntLiteral IL;

    public U32Expr(IntLiteral ilAST, Position pos) {
        super(pos);
        IL = ilAST;
        IL.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitU32Expr(this, o);
    }
    
}

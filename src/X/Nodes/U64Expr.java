package X.Nodes;

import X.Lexer.Position;

public class U64Expr extends Expr {

    public IntLiteral IL;

    public U64Expr(IntLiteral ilAST, Position pos) {
        super(pos);
        IL = ilAST;
        IL.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitU64Expr(this, o);
    }
    
}

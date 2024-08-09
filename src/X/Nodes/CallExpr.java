package X.Nodes;

import X.Lexer.Position;

public class CallExpr extends Expr {

    public Ident I;
    public List AL;
    public String TypeDef;


    public void setTypeDef(String X) {
        TypeDef = X;
    }

    public CallExpr(Ident id, List alAST, Position pos) {
        super(pos);
        I = id;
        AL = alAST;
        I.parent = AL.parent = this;
    }

    @Override
    public Object visit(Visitor v, Object o) {
        return v.visitCallExpr(this, o);
    }
}

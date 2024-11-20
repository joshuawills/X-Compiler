package X.Nodes;

import X.Lexer.Position;

public class CallExpr extends Expr {

    public Ident I;
    public List AL;
    public String TypeDef;

    public boolean isLibC;

    public void setTypeDef(String X) {
        TypeDef = X;
    }

    public CallExpr(Ident id, List alAST, Position pos, boolean isLibCV) {
        super(pos);
        I = id;
        AL = alAST;
        isLibC = isLibCV;
        I.parent = AL.parent = this;
    }

    @Override
    public Object visit(Visitor v, Object o) {
        return v.visitCallExpr(this, o);
    }
}

package X.Nodes;

import X.Lexer.Position;

public class VarExpr extends Expr {

    public Var V;

    public VarExpr(Var vAST, Position pos) {
        super(pos);
        V = vAST;
        V.parent = this;
    }

    @Override
    public Object visit(Visitor v, Object o) {
        return v.visitVarExpr(this, o);
    }
}

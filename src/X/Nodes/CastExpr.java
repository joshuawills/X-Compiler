package X.Nodes;

import X.Lexer.Position;

public class CastExpr extends Expr {

    public Expr E;
    public Type tFrom, tTo;

    public CastExpr(Expr eAST, Type tFromAST, Type tToAST, Position pos) {
        super(pos);
        E = eAST;
        tFrom = tFromAST;
        tTo = tToAST;
        E.parent = this;
    }


    public CastExpr(Expr eAST, Type tFromAST, Type tToAST, Position pos, AST p) {
        super(pos);
        E = eAST;
        tFrom = tFromAST;
        tTo = tToAST;
        E.parent = this;
        this.parent = p;
    }
    public Object visit(Visitor v, Object o) {
        return v.visitCastExpr(this, o);
    }
}

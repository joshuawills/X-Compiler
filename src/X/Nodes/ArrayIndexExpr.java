package X.Nodes;

import X.Lexer.Position;

public class ArrayIndexExpr extends Expr {

    public Ident I;
    public Expr index;

    public Type parentType;

    public ArrayIndexExpr(Ident iAST, Expr eAST, Position pos) {
        super(pos);
        I = iAST;
        index = eAST;
        I.parent = index.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitArrayIndexExpr(this, o);
    }
}
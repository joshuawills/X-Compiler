package X.Nodes;

import X.Lexer.Position;

public class TupleAccess extends Expr {

    public Ident I;
    public IntExpr index;
    public TupleType ref;

    public SimpleVar V;

    public TupleAccess(Ident iAST, IntExpr indexAST, Position pos) {
        super(pos);
        I = iAST;
        index = indexAST;
        I.parent = index.parent = this;
    }

    public Object visit(Visitor v, Object arg) {
        return v.visitTupleAccess(this, arg);
    }
    
}

package X.Nodes;

import X.Lexer.Position;

public class TupleExprList extends List {

    public Expr E;
    public List EL;

    public TupleExprList(Expr E, List EL, Position pos) {
        super(pos);
        this.E = E;
        this.EL = EL;
        EL.parent = E.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitTupleExprList(this, o);
    }
    
}

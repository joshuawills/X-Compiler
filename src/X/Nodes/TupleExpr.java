package X.Nodes;

import X.Lexer.Position;

public class TupleExpr extends Expr {

    public List EL;

    public TupleExpr(List elAST, Position pos) {
        super(pos);
        EL = elAST;
        EL.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitTupleExpr(this, o);
    }
    
}

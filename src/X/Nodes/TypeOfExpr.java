package X.Nodes;

import X.Lexer.Position;

public class TypeOfExpr extends Expr {
    
    public Expr E;
    public StringExpr SE;

    public TypeOfExpr(Expr expr, Position pos) {
        super(pos);
        this.E = expr;
        expr.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitTypeOfExpr(this, o);
    }
    
}

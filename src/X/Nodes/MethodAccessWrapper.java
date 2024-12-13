package X.Nodes;

public class MethodAccessWrapper extends Expr {

    public MethodAccessExpr methodAccessExpr;

    public MethodAccessWrapper(MethodAccessExpr methodAccessExpr) {
        super(methodAccessExpr.pos);
        this.methodAccessExpr = methodAccessExpr;
        methodAccessExpr.parent = this;
    }
    
    public Object visit(Visitor v, Object o) {
        return v.visitMethodAccessWrapper(this, o);
    }
    
}

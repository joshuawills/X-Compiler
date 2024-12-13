package X.Nodes;

import X.Lexer.Position;

public class MethodAccessExpr extends Expr {

    public Ident I;
    public List args;
    public Expr next;

    public SimpleVar refVar = null;
    
    public Method ref;

    public String TypeDef = null;

    public MethodAccessExpr(Ident iAST, List argsAST, Position pos, Expr nextAST) {
        super(pos);
        next = nextAST;
        I = iAST;
        args = argsAST;
        next.parent = I.parent = args.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitMethodAccessExpr(this, o);
    }
    
}

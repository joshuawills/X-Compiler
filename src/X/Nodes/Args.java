package X.Nodes;

import X.Lexer.Position;

public class Args extends List {

    public Expr E;
    public List EL;

    public Args(Expr eAST, List elAST, Position pos) {
        super(pos);
        E = eAST;
        EL = elAST;
        E.parent = EL.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitArgList(this, o);
    }
}

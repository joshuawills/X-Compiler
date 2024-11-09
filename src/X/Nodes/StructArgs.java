package X.Nodes;

import X.Lexer.Position;

public class StructArgs extends List {

    public Expr E;
    public int structIndex;
    public List SL;

    public int parentIndex; // for struct specialties

    public StructArgs(Expr eAST, List slAST, Position pos) {
        super(pos);
        E = eAST;
        SL = slAST;
        E.parent = SL.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitStructArgs(this, o);
    }
}

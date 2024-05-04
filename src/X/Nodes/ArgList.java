package X.Nodes;

import X.Lexer.Position;

public class ArgList extends List {

    public Expr E;
    public List AL;

    public ArgList(Expr eAST, List alAST, Position pos) {
        super(pos);
        E = eAST;
        AL = alAST;
        E.parent = AL.parent = this;
    }

    @Override
    public Object visit(Visitor v, Object o) {
        return v.visitArgList(this, o);
    }
}

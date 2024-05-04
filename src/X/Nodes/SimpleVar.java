package X.Nodes;

import X.Lexer.Position;


public class SimpleVar extends Var {

    public Ident I;

    public SimpleVar(Ident idAST, Position pos) {
        super(pos);
        I = idAST;
        I.parent = this;
    }

    @Override
    public Object visit(Visitor v, Object o) {
        return v.visitSimpleVar(this, o);
    }
}

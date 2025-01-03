package X.Nodes;

import X.Lexer.Position;

public class GenericTypeList extends List {

    public Ident I;
    public List IL; // Implements List


    public List GTL;

    public GenericTypeList(Ident iAST, List ilAST, List gtlAST, Position pos) {
        super(pos);
        I = iAST;
        IL = ilAST;
        GTL = gtlAST;
        IL.parent = I.parent = GTL.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitGenericTypeList(this, o);
    }
}

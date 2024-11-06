package X.Nodes;

import X.Lexer.Position;

public class StructAccessList extends List {

    public Ident SA;
    public List SAL;
    public Struct ref;

    public StructAccessList(Ident saAST, List salAST, Position pos) {
        super(pos);
    SA = saAST;
        SAL = salAST;
        SA.parent = SAL.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitStructAccessList(this, o);
    }
}

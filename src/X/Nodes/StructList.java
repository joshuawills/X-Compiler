package X.Nodes;

import X.Lexer.Position;

public class StructList extends List {

    public StructElem S;
    public List SL;

    public StructList(StructElem sAST, List slAST, Position pos) {
        super(pos);
        S = sAST;
        SL = slAST;
        S.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitStructList(this, o);
    }

}
package X.Nodes;

import X.Lexer.Position;

public class EmptyStructAccessList extends List {

    public EmptyStructAccessList(Position pos) {
        super(pos);
    }

    public Object visit(Visitor v, Object o) {
        return v.visitEmptyStructAccessList(this, o);
    }

}

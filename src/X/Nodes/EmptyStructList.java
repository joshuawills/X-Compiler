package X.Nodes;

import X.Lexer.Position;

public class EmptyStructList extends List {

    public EmptyStructList(Position pos) {
        super(pos);
    }

    public Object visit(Visitor v, Object o) {
        return v.visitEmptyStructList(this, o);
    }
}

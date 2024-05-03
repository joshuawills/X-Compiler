package X.Nodes;

import X.Lexer.Position;

public class EmptyStmtList extends List {

    public EmptyStmtList(Position pos) {
        super(pos);
    }

    public Object visit(Visitor v, Object o) {
        return v.visitEmptyStmtList(this, o);
    }

}


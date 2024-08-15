package X.Nodes;

import X.Lexer.Position;

public class EmptyStructArgs extends List {

    public EmptyStructArgs(Position pos) {
        super(pos);
    }

    public Object visit(Visitor v, Object o) {
        return v.visitEmptyStructArgs(this, o);
    }
}

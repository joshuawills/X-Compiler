package X.Nodes;

import X.Lexer.Position;

public class EmptyArgList extends List {

    public EmptyArgList(Position pos) {
        super(pos);
    }

    @Override
    public Object visit(Visitor v, Object o) {
        return v.visitEmptyArgList(this, o);
    }
}

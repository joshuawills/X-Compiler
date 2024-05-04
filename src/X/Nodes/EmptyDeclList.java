package X.Nodes;

import X.Lexer.Position;

public class EmptyDeclList extends List {
    public EmptyDeclList(Position pos) {
        super(pos);
    }

    @Override
    public Object visit(Visitor v, Object o) {
        return v.visitEmptyDeclList(this, o);
    }
}

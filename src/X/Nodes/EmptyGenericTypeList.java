package X.Nodes;

import X.Lexer.Position;

public class EmptyGenericTypeList extends List {

    public EmptyGenericTypeList(Position pos) {
        super(pos);
    }

    public Object visit(Visitor v, Object o) {
        return v.visitEmptyGenericTypeList(this, o);
    }
    
}

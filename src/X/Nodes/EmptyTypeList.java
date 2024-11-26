package X.Nodes;

import X.Lexer.Position;

public class EmptyTypeList extends List {
    
    public EmptyTypeList(Position pos) {
        super(pos);
    }

    public Object visit(Visitor v, Object o) {
        return v.visitEmptyTypeList(this, o);
    }

    @Override
    public String toString() {
        return "";
    }

    public boolean equals(Object obj) {
        return obj instanceof EmptyTypeList;
    }
    
}

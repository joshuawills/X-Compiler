package X.Nodes;

import X.Lexer.Position;

public class EmptyParaList extends List {

    public EmptyParaList(Position pos) {
        super(pos);
    }

    public Object visit(Visitor v, Object o) {
        return v.visitEmptyParaList(this, o);
    }

    public boolean equals(Object o) {
        return o instanceof EmptyParaList
            || o instanceof EmptyArgList;
    }

}

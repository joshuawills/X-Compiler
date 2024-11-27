package X.Nodes;

import X.Lexer.Position;

public class TypeList extends List {

    public Type T;
    public List TL;

    public TypeList(Type T, List Tl, Position pos) {
        super(pos);
        this.T = T;
        this.TL = Tl;
        TL.parent = T.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitTypeList(this, o);
    }

    @Override 
    public String toString() {
        if (TL.isEmptyTypeList()) {
            return T.toString();
        }
        return T.toString() + ", " + TL.toString();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof TypeList)) {
            return false;
        }
        TypeList t = (TypeList) obj;
        return T.equals(t.T) && TL.equals(t.TL);
    }

}

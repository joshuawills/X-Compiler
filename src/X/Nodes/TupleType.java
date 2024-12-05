package X.Nodes;

import X.Lexer.Position;

public class TupleType extends Type {

    public List TL;
    public boolean cachedMurky = false;
    public int index = 0;

    public boolean inAnotherTupleType = false;

    public TupleType(List tlAST, Position pos) {
        super(pos);
        TL = tlAST;
        TL.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitTupleType(this, o);
    }

    @Override
    public String toString() {
        return "(" + TL.toString() + ")";
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof TupleType)) {
            return false;
        }
        TupleType t = (TupleType) obj;
        return TL.equals(t.TL);
    }

    public int getLength() {
        int length = 0;

        if (TL.isEmptyTypeList()) {
            return 0;
        }
        TypeList TL2 = (TypeList) TL;
        while (true) {
            length++;
            if (TL2.TL.isEmptyTypeList()) {
                break;
            }
            TL2 = (TypeList) TL2.TL;
        }

        return length;
    }

    public Type getNthType(int n) {
        if (n < 0) {
            return null;
        }

        TypeList TL2 = (TypeList) TL;
        for (int i = 0; i < n; i++) {
            if (TL2.TL.isEmptyTypeList()) {
                return null;
            }
            TL2 = (TypeList) TL2.TL;
        }

        return TL2.T;
    }

    public boolean assignable(Object obj) {
        if (!(obj instanceof TupleType)) {
            return false;
        }
        TupleType t = (TupleType) obj;
        if (TL.isEmptyTypeList() && t.TL.isEmptyTypeList()) {
            return true;
        } else if (TL.isEmptyTypeList() || t.TL.isEmptyTypeList()) {
            return false;
        }

        TypeList TL2 = (TypeList) TL;
        TypeList TL3 = (TypeList) t.TL;
        while (!TL2.isEmptyTypeList() && !TL3.isEmptyTypeList()) {
            Type currentType = TL2.T;
            Type otherType = TL3.T;

            if (!currentType.assignable(otherType)) {
                return false;
            }

            if (TL2.TL.isEmptyTypeList() && TL3.TL.isEmptyTypeList()) {
                return true;
            } else if (TL2.TL.isEmptyTypeList() || TL3.TL.isEmptyTypeList()) {
                return false;
            }

            TL2 = (TypeList) TL2.TL;
            TL3 = (TypeList) TL3.TL;
        }

        return true;
    }

    // TODO: may need to update
    public String getMini() {
        return "tuple." + index;
    }

}

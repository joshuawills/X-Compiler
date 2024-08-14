package X.Nodes;

import X.Lexer.Position;

public class StructType extends Type {

    // Reference to the 'Enum'
    // Includes name and members
    public Struct S;

    public StructType(Struct sASt, Position pos) {
        super(pos);
        S = sASt;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitStructType(this, o);
    }

    public String toString() {
        return "struct." + S.I.spelling;
    }

    public String getMini() {
        return "S." + S.I.spelling;
    }

    public boolean equals(Object obj) {
        if (obj instanceof ErrorType) {
            return true;
        } else if (obj instanceof StructType SA) {
            return S.I.spelling.equals(SA.S.I.spelling);
        }
        return false;
    }

    public boolean assignable(Object obj) {
        return equals(obj);
    }

}

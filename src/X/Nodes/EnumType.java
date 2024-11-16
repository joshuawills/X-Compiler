package X.Nodes;

import X.Lexer.Position;

public class EnumType extends Type {

    // Reference to the 'Enum'
    // Includes name and members
    public Enum E;

    public EnumType(Enum eAST, Position pos) {
        super(pos);
        E = eAST;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitEnumType(this, o);
    }

    public String toString() {
        return "enum." + E.I.spelling;
    }

    public String getMini() {
        return "E." + E.I.spelling;
    }

    public boolean equals(Object obj) {
        if (obj instanceof ErrorType) {
            return true;
        } else {
            return obj instanceof EnumType;
        }
    }

    public boolean assignable(Object obj) {
        return equals(obj) || obj instanceof I64Type || obj instanceof I8Type;
    }

}

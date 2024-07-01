package X.Nodes;

import X.Lexer.Position;

public class FloatLiteral extends Terminal {

    public FloatLiteral(String value, Position pos) {
        super(value, pos);
    }

    public Object visit(Visitor v, Object o) {
        return v.visitFloatLiteral(this, o);
    }
}

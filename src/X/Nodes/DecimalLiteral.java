package X.Nodes;

import X.Lexer.Position;

public class DecimalLiteral extends Terminal {

    public DecimalLiteral(String value, Position pos) {
        super(value, pos);
    }

    public Object visit(Visitor v, Object o) {
        return v.visitDecimalLiteral(this, o);
    }
}

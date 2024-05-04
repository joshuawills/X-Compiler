package X.Nodes;

import X.Lexer.Position;

public class BooleanLiteral extends Terminal {

    public BooleanLiteral(String value, Position pos) {
        super(value, pos);
    }

    public Object visit(Visitor v, Object o) {
        return v.visitBooleanLiteral(this, o);
    }
}

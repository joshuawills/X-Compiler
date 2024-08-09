package X.Nodes;

import X.Lexer.Position;

public class StringLiteral extends Terminal {

    public StringLiteral(String value, Position pos) {
        super(value, pos);
    }

    public Object visit(Visitor v, Object o) {
        return v.visitStringLiteral(this, o);
    }
}
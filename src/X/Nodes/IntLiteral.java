package X.Nodes;

import X.Lexer.Position;

public class IntLiteral extends Terminal {

    public IntLiteral(String value, Position pos) {
        super(value, pos);
    }

    public Object visit(Visitor v, Object o) {
        return v.visitIntLiteral(this, o);
    }
}

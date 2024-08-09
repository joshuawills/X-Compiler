package X.Nodes;

import X.Lexer.Position;

public class CharLiteral extends Terminal {

    public CharLiteral(String value, Position pos) {
        super(value, pos);
    }

    public Object visit(Visitor v, Object o) {
        return v.visitCharLiteral(this, o);
    }

}

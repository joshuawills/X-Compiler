package X.Nodes;

import X.Lexer.Position;

public class Operator extends Terminal {

    public Operator(String value, Position pos) {
        super(value, pos);
    }

    public Object visit(Visitor v, Object o) {
        return v.visitOperator(this, o);
    }

}

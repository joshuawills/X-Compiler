package X.Nodes;

import X.Lexer.Position;

public class Ident extends Terminal {

    public AST decl;

    public Ident(String spelling, Position pos) {
        super(spelling, pos);
        decl = null;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitIdent(this, o);
    }

}

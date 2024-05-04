package X.Nodes;

import X.Lexer.Position;

public class Program extends AST {

    public List PL;

    public Program(List PL, Position pos) {
        super(pos);
        this.PL = PL;
        PL.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitProgram(this, o);
    }

}

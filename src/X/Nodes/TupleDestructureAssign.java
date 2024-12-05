package X.Nodes;

import X.Lexer.Position;

public class TupleDestructureAssign extends Decl {

    public List idents;
    public Expr E;

    public TupleDestructureAssign(List idents, Expr expr, Type tAST, Position pos, boolean isMutAST) {
        super(pos, isMutAST);
        T = tAST;
        this.idents = idents;
        this.E = expr;
        T.parent = E.parent = idents.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitTupleDestructureAssign(this, o);
    }

    public int getLength() {
        int size = 0;
        List IL = idents;
        while (true) {
            if (IL instanceof IdentsList) {
                size++;
                IL = ((IdentsList) IL).IL;
            } else {
                break;
            }
        }
        return size;
    }
    
}

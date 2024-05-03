package X.Nodes;

import X.Lexer.Position;

public class Function extends Decl {
    
    public List PL; // Parameter List
    public Stmt S; // Should always be compound, or empty

    public Function(Type tAST, Ident idAST, List fplAST, Stmt cAST, Position pos) {
        super(pos);
        T = tAST;
        I = idAST;
        PL = fplAST;
        S = cAST;
        T.parent = I.parent = PL.parent = S.parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitFunction(this, o);
    }

}

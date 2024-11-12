package X.Nodes;

import X.Lexer.Position;

import java.util.ArrayList;

public class Function extends Decl {

    public List PL; // Parameter List
    public Stmt S; // Should always be compound, or empty
    public String TypeDef;

    public void setTypeDef() {

        List head = PL;
        ArrayList<String> options = new ArrayList<>();
        while (!(head instanceof EmptyParaList)) {
            ParaDecl D = ((ParaList) head).P;
            options.add(D.T.getMini());

            head = ((ParaList) head).PL;
        }
        TypeDef  = String.join("_", options);
    }

    public Function(Type tAST, Ident idAST, List fplAST, Stmt cAST, Position pos) {
        super(pos, false);
        T = tAST;
        I = idAST;
        PL = fplAST;
        S = cAST;
        setTypeDef();
        T.parent = I.parent = PL.parent = this;
        if (S != null) {
            S.parent = this;
        }
    }

    public void setUsed() {
        this.isUsed = true;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitFunction(this, o);
    }

}

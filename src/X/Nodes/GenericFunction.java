package X.Nodes;

import X.Lexer.Position;

public class GenericFunction extends Decl {

    public List PL; // Parameter list
    public Stmt S; // Should always be compound, or empty
    public List GTL; // Generics type list

    public GenericFunction(Type tAST, Ident idAST, List fplAST, List gtlAST, Stmt cAST, Position pos) {
        super(pos, false);
        T = tAST;
        I = idAST;
        PL = fplAST;
        S = cAST;
        GTL = gtlAST;
        T.parent = I.parent = PL.parent
            = S.parent = GTL.parent = this;
    }

    public void setUsed() {
        this.isUsed = true;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitGenericFunction(this, o);
    }

    public boolean equalTypeParameters(List providedL) {
        boolean isArgs = providedL.isEmptyArgList() || providedL.isArgs();
        
        if (isArgs) {
            if (providedL.isEmptyArgList() && this.PL.isEmptyParaList()) {
                return true;
            } else if (providedL.isEmptyArgList() || this.PL.isEmptyParaList()) {
                return false;
            }

            Args provided = (Args) providedL;
            ParaList real = (ParaList) this.PL;
            while (true) {
                if (!real.P.T.equals(provided.E.type)) {
                    return false;
                }
                if (real.PL.isEmptyParaList() && provided.EL.isEmptyArgList()) {
                    return true;
                } else if (real.PL.isEmptyParaList() || provided.EL.isEmptyArgList()) {
                    return false;
                }
                provided = (Args) provided.EL;
                real = (ParaList) real.PL;
            } 
        } else {
            if (providedL.isEmptyParaList() && this.PL.isEmptyParaList()) {
                return true;
            } else if (providedL.isEmptyParaList() || this.PL.isEmptyParaList()) {
                return false;
            }

            ParaList provided = (ParaList) providedL;
            ParaList real = (ParaList) this.PL;
            while (true) {
                if (!real.P.T.equals(provided.P.T)) {
                    return false;
                }

                if (real.PL.isEmptyParaList() && provided.PL.isEmptyParaList()) {
                    return true;
                } else if (real.PL.isEmptyParaList() || provided.PL.isEmptyParaList()) {
                    return false;
                }
                provided = (ParaList) provided.PL;
                real = (ParaList) real.PL;
            } 

        }
    }
    
}

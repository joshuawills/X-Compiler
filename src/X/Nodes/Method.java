package X.Nodes;

import java.util.ArrayList;

import X.Lexer.Position;

public class Method extends Decl {

    public ParaDecl attachedStruct;

    public List PL; // Parameter list
    public Stmt S; // Should always be compound, or empty

    public String TypeDef;

    public String filename;

    public boolean isTraitFunction = false;

    public Method(Type tAST, Ident idAST, List fplAST, Stmt cAST, ParaDecl asASt, Position pos) {
        super(pos, false);
        T = tAST;
        I = idAST;
        PL = fplAST;
        S = cAST;
        attachedStruct = asASt;
        T.parent = I.parent = PL.parent = this;
        if (S != null) {
            S.parent = this;
        }
    }

    public Object visit(Visitor v, Object o) {
        return v.visitMethod(this, o);
    }

    public void setTypeDef() {

        List head = PL;
        ArrayList<String> options = new ArrayList<>();

        while (!(head instanceof EmptyParaList)) {
            ParaDecl D = ((ParaList) head).P;
            options.add(D.T.getMini());

            head = ((ParaList) head).PL;
        }
        TypeDef = String.join("_", options);
    }

    public void setUsed() {
        this.isUsed = true;
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

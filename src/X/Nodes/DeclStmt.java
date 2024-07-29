package X.Nodes;

import X.Lexer.Position;

import java.util.Optional;

public class DeclStmt extends Stmt {

    public Ident I;
    public Expr E;
    public boolean isDeref = false;
    public DeclOptions opt;
    public Optional<Expr> aeAST = Optional.empty();

    public DeclStmt(Ident iAST, Expr eAST, Position pos, boolean iD) {
        super(pos);
        I = iAST;
        E = eAST;
        isDeref = iD;
        opt = DeclOptions.STANDARD;
        I.parent = E.parent = this;
    }

    public DeclStmt(Ident iAST, Expr eAST, Position pos, boolean iD, Expr aA) {
        super(pos);
        I = iAST;
        E = eAST;
        isDeref = iD;
        opt = DeclOptions.ARRAY_ACC;
        aeAST = Optional.ofNullable(aA);
        I.parent = E.parent = aeAST.get().parent = this;
    }

    public Object visit(Visitor v, Object o) {
        return switch (opt) {
            case STANDARD -> v.visitDeclStmt(this, o);
            case ARRAY_ACC -> v.visitArrDeclStmt(this, o);
        };
    }

}

package X.Nodes;

import X.Lexer.Position;

public abstract class AST {

    public Position pos;
    public AST parent;

    public AST(Position pos) {
        this.pos = pos;
    }

    public Position getPosition() {
        return this.pos;
    }

    public abstract Object visit(Visitor v, Object o);

    public boolean isLocalVar() {
        return this instanceof LocalVar;
    }

    public boolean isGlobalVar() {
        return this instanceof GlobalVar;
    }

    public boolean isParaDecl() {
        return this instanceof ParaDecl;
    }

    public boolean isEmptyArgList() {
        return this instanceof EmptyArgList;
    }

    public boolean isEmptyParaList() {
        return this instanceof EmptyParaList;
    }

    public boolean isEmptyStmtList() {
        return this instanceof EmptyStmtList;
    }

    public boolean isStructList() {
        return this instanceof StructList;
    }

    public boolean isEmptyStructAccessList() {
        return this instanceof EmptyStructAccessList;
    }

    public boolean isStructAccessList() {
        return this instanceof StructAccessList;
    }

    public boolean isEmptyStructArgs() {
        return this instanceof EmptyStructArgs;
    }

    public boolean isStmtList() {
        return this instanceof StmtList;
    }

    public boolean isArgs() {
        return this instanceof Args;
    }

    public boolean isStructArgs() {
        return this instanceof StructArgs;
    }

    public boolean isEnum() {
        return this instanceof Enum;
    }

    public boolean isAssignmentExpr() {
        return this instanceof AssignmentExpr;
    }

    public boolean isCallExpr() {
        return this instanceof CallExpr;
    }

    public boolean isEmptyStructList() {
        return this instanceof EmptyStructList;
    }

    public boolean isEmptyDeclList() {
        return this instanceof EmptyDeclList;
    }

    public boolean isArrayInitExpr() {
        return this instanceof ArrayInitExpr;
    }

    public boolean isStructAccess() {
        return this instanceof StructAccess;
    }

}

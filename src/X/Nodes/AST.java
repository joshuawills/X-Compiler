package X.Nodes;

import X.Lexer.Position;

public abstract class AST {

    public Position pos;
    public AST parent;

    public boolean inDeclaringLocalVar = false;
    public boolean inCallExpr = false;

    public AST(Position pos) {
        this.pos = pos;
    }

    public Position getPosition() {
        return this.pos;
    }

    public abstract Object visit(Visitor v, Object o);

    public void setDeclaringLocalVar() {
        inDeclaringLocalVar = true;
    } 

    public void setInCallExpr() {
        inCallExpr = true;
    }

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

    public boolean isParaList() {
        return this instanceof ParaList;
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

    public boolean isImportStmt() {
        return this instanceof ImportStmt;
    }

    public boolean isIntExpr() {
        return this instanceof IntExpr;
    }

    public boolean isDecimalExpr() {
        return this instanceof DecimalExpr;
    }
    
    public boolean isIntOrDecimalExpr() {
        return this instanceof IntExpr || this instanceof DecimalExpr;
    }
    
    public boolean isNullExpr() {
        return this instanceof NullExpr;
    }

    public boolean isBinaryExpr() {
        return this instanceof BinaryExpr;
    }

    public boolean isEmptyTypeList() {
        return this instanceof EmptyTypeList;
    }

    public boolean isTypeList() {
        return this instanceof TypeList;
    }

    public boolean isTupleExpr() {
        return this instanceof TupleExpr;
    }

    public boolean isReturnStmt() {
        return this instanceof ReturnStmt;
    }

    public boolean isIdentsList() {
        return this instanceof IdentsList;
    }

    public boolean isDerefExpr() {
        return this instanceof DerefExpr;
    }

    public boolean isIdent() {
        return this instanceof Ident;
    }

    public boolean isMethodAccessExpr() {
        return this instanceof MethodAccessExpr;
    }

    public boolean isEmptyTraitList() {
        return this instanceof EmptyTraitList;
    }

    public boolean isTrait() {
        return this instanceof Trait;
    }

    public boolean isImpl() {
        return this instanceof Impl;
    }

    public boolean isUsingStmt() {
        return this instanceof UsingStmt;
    }

    public boolean isEmptyTupleExprList() {
        return this instanceof EmptyTupleExprList;
    }

    public boolean isEmptyIdentsList() {
        return this instanceof EmptyIdentsList;
    }

    public boolean isEmptyMethodList() {
        return this instanceof EmptyMethodList;
    }

    public boolean isGenericTypeList() {
        return this instanceof GenericTypeList;
    }

    public boolean isEmptyGenericTypeList() {
        return this instanceof EmptyGenericTypeList;
    }

    public boolean isEmptyImplementsList() {
        return this instanceof EmptyImplementsList;
    }
}

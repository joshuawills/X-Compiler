package X.CodeGen;

import X.ErrorHandler;
import X.Nodes.*;

public class Emitter implements Visitor {

    private ErrorHandler handler;
    private final String outputName;

    public Emitter(String outputName, ErrorHandler handler) {
        this.handler = handler;
        this.outputName = outputName;
    }

    public final void gen(AST ast) {
        ast.visit(this, null);
        LLVM.dump(outputName);
    }

    public Object visitProgram(Program ast, Object o) {
        DeclList l = (DeclList) ast.PL;
        while (true) {
            l.D.visit(this, o);
            if (l.DL instanceof EmptyDeclList) {
                break;
            }
            l = (DeclList) l.DL;
        }
        return null;
    }

    public Object visitIdent(Ident ast, Object o) {
        return null;
    }

    public Object visitFunction(Function ast, Object o) {

        emit("define ");
        ast.T.visit(this, o);
        emit(" @" + ast.I.spelling);
        ast.PL.visit(this, o);
        emitN(" {");
        ast.S.visit(this, o);
        emitN("}");
        return null;
    }

    public Object visitGlobalVar(GlobalVar ast, Object o) {
        emit("@" + ast.I.spelling + "= external global ");
        ast.T.visit(this, o);
        ast.E.visit(this, o);
        emitN("");
        return null;
    }

    public Object visitCompoundStmt(CompoundStmt ast, Object o) {
        return null;
    }

    public Object visitLocalVar(LocalVar ast, Object o) {
        return null;
    }

    public Object visitIfStmt(IfStmt ast, Object o) {
        return null;
    }

    public Object visitElseIfStmt(ElseIfStmt ast, Object o) {
        return null;
    }

    public Object visitForStmt(ForStmt ast, Object o) {
        return null;
    }

    public Object visitWhileStmt(WhileStmt ast, Object o) {
        return null;
    }

    public Object visitBreakStmt(BreakStmt ast, Object o) {
        return null;
    }

    public Object visitContinueStmt(ContinueStmt ast, Object o) {
        return null;
    }

    public Object visitReturnStmt(ReturnStmt ast, Object o) {
        return null;
    }

    public Object visitDeclStmt(DeclStmt ast, Object o) {
        return null;
    }

    public Object visitOperator(Operator ast, Object o) {
        return null;
    }

    public Object visitBinaryExpr(BinaryExpr ast, Object o) {
        return null;
    }

    public Object visitUnaryExpr(UnaryExpr ast, Object o) {
        return null;
    }

    public Object visitEmptyExpr(EmptyExpr ast, Object o) {
        return null;
    }

    public Object visitEmptyStmt(EmptyStmt ast, Object o) {
        return null;
    }

    public Object visitDeclList(DeclList ast, Object o) {
        return null;
    }

    public Object visitStmtList(StmtList ast, Object o) {
        return null;
    }

    public Object visitEmptyStmtList(EmptyStmtList ast, Object o) {
        return null;
    }

    public Object visitBooleanExpr(BooleanExpr ast, Object o) {
        return null;
    }

    public Object visitBooleanLiteral(BooleanLiteral ast, Object o) {
        return null;
    }

    public Object visitBooleanType(BooleanType ast, Object o) {
        emit(LLVM.BOOL_TYPE);
        return null;
    }

    public Object visitVoidType(VoidType ast, Object o) {
        emit(LLVM.VOID_TYPE);
        return null;
    }

    public Object visitErrorType(ErrorType ast, Object o) {
        return null;
    }

    public Object visitIntExpr(IntExpr ast, Object o) {
        return null;
    }

    public Object visitIntLiteral(IntLiteral ast, Object o) {
        return null;
    }

    public Object visitIntType(IntType ast, Object o) {
        emit(LLVM.INT_TYPE);
        return null;
    }

    public Object visitArgList(Args ast, Object o) {
        return null;
    }

    public Object visitParaList(ParaList ast, Object o) {
        emit("(");
        List list = ast;
        while (true) {
            ParaList PL = (ParaList) list;
            PL.P.visit(this, o);
            list = PL.PL;
            if (!(list instanceof EmptyParaList)) {
                emit(", ");
            } else {
                break;
            }
        }
        emit(")");
        return null;
    }

    public Object visitStringExpr(StringExpr ast, Object o) {
        return null;
    }

    public Object visitStringLiteral(StringLiteral ast, Object o) {
        return null;
    }

    public Object visitStringType(StringType ast, Object o) {
        emit("TODO");
        return null;
    }

    public Object visitEmptyParaList(EmptyParaList ast, Object o) {
        emit("()");
        return null;
    }

    public Object visitParaDecl(ParaDecl ast, Object o) {
        return null;
    }

    public Object visitEmptyCompStmt(EmptyCompStmt ast, Object o) {
        return null;
    }

    public Object visitEmptyDeclList(EmptyDeclList ast, Object o) {
        return null;
    }

    public Object visitVarExpr(VarExpr ast, Object o) {
        return null;
    }

    public Object visitSimpleVar(SimpleVar ast, Object o) {
        return null;
    }

    public Object visitCallExpr(CallExpr ast, Object o) {
        return null;
    }

    public Object visitEmptyArgList(EmptyArgList ast, Object o) {
        return null;
    }

    public Object visitLocalVarStmt(LocalVarStmt ast, Object o) {
        return null;
    }

    public Object visitCallStmt(CallStmt ast, Object o) {
        return null;
    }

    public String getType(Type t) {
        return "";
    }

    public void emit(String s) {
        LLVM.append(new Instruction(s));
    }

    public void emitN(String s) {
        LLVM.append(new Instruction(s + "\n"));
    }
}


package X.TreePrinter;

import X.Nodes.*;
import X.Nodes.Enum;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Arrays;

public class Printer implements Visitor {

    private int indent;
    private PrintWriter textOut;

    public Printer(String filename) {
        indent = 0;
        try {
            textOut = new PrintWriter(new FileWriter(filename));
        } catch (Exception e) {
            System.out.println("Unable to create PrintWriter");
        }
    }

    private String indentString() {
        return "  ".repeat(Math.max(0, indent));
    }

    void print(String s) {
        textOut.println(s);
    }

    public final void print(AST ast) {
        ast.visit(this, null);
        textOut.close();
    }

    @Override
    public Object visitProgram(Program ast, Object o) {
        print(indentString() + "Program");
        ++indent;
        ast.PL.visit(this, o);
        --indent;
        return null;
    }

    @Override
    public Object visitIdent(Ident ast, Object o) {
        print(indentString() + ast.spelling);
        return null;
    }

    @Override
    public Object visitFunction(Function ast, Object o) {
        print(indentString() + "Function");
        ++indent;
        ast.T.visit(this, o);
        ast.I.visit(this, o);
        ast.PL.visit(this, o);
        ast.S.visit(this, o);
        --indent;
         return null;
    }

    @Override
    public Object visitGlobalVar(GlobalVar ast, Object o) {
        print(indentString() + "GlobalVar");
        ++indent;
        ast.T.visit(this, o);
        ast.I.visit(this, o);
        if (! (ast.E instanceof EmptyExpr)) {
            print(indentString() + "=");
            ast.E.visit(this, o);
        }
        --indent;
        return null;
    }

    @Override
    public Object visitCompoundStmt(CompoundStmt ast, Object o) {
        print(indentString() + "CompoundStmt");
        ++indent;
        ast.SL.visit(this, o);
        --indent;
        return null;
    }

    @Override
    public Object visitLocalVar(LocalVar ast, Object o) {
        print(indentString() + "LocalVarDecl");
        ++indent;
        ast.T.visit(this, o);
        ast.I.visit(this, o);
        if (! (ast.E instanceof EmptyExpr)) {
            print(indentString() + "=");
            ast.E.visit(this, o);
        }
        --indent;
        return null;
    }

    @Override
    public Object visitIfStmt(IfStmt ast, Object o) {
        print(indentString() + "IfStmt");
        ++indent;
        ast.E.visit(this, o);
        ast.S1.visit(this, null);
        ast.S2.visit(this, null);
        ast.S3.visit(this, null);
        --indent;
        return null;
    }

    @Override
    public Object visitElseIfStmt(ElseIfStmt ast, Object o) {
        print(indentString() + "IfStmt");
        ++indent;
        ast.E.visit(this, o);
        ast.S1.visit(this, null);
        ast.S2.visit(this, null);
        --indent;
        return null;
    }

    @Override
    public Object visitForStmt(ForStmt ast, Object o) {
        print(indentString() + "ForStmt");
        ++indent;
        ast.S1.visit(this, o);
        ast.E2.visit(this, o);
        ast.S3.visit(this, o);
        ast.S.visit(this, o);
        --indent;
        return null;
    }

    @Override
    public Object visitWhileStmt(WhileStmt ast, Object o) {
        print(indentString() + "WhileStmt");
        ++indent;
        ast.E.visit(this, o);
        ast.S.visit(this, o);
        --indent;
        return null;
    }

    @Override
    public Object visitDoWhileStmt(DoWhileStmt ast, Object o) {
        print(indentString() + "DoWhileStmt");
        ++indent;
        ast.S.visit(this, o);
        ast.E.visit(this, o);
        --indent;
        return null;
    }

    public Object visitFloatLiteral(FloatLiteral ast, Object o) {
        print(indentString() + ast.spelling);
        return null;
    }

    public Object visitFloatType(FloatType ast, Object o) {
        print(indentString() + "float");
        return null;
    }

    public Object visitFloatExpr(FloatExpr ast, Object o) {
        print(indentString() + "FloatExpr");
        ++indent;
        ast.FL.visit(this, o);
        --indent;
        return null;
    }

    public Object visitPointerType(PointerType ast, Object o) {
        print(indentString() + ast.t.toString() + " *");
        return null;
    }

    public Object visitArrayType(ArrayType ast, Object o) {
        print(indentString() + ast.t.toString() + "[]");
        return null;
    }

    public Object visitArrayInitExpr(ArrayInitExpr ast, Object o) {
        print(indentString() + "ArrayInitExpr");
        ++indent;
        ast.AL.visit(this, o);
        --indent;
        return null;
    }

    public Object visitArrayIndexExpr(ArrayIndexExpr ast, Object o) {
        print(indentString() + ast.I.spelling + "[");
        ast.index.visit(this, o);
        print("]");
        return null;
    }

    public Object visitArrDeclStmt(DeclStmt ast, Object o) {
        print(indentString() + "ArrDeclStmt");
        ast.I.visit(this, o);
        ast.aeAST.get().visit(this, o);
        ast.E.visit(this, o);
        return null;
    }

    public Object visitCharType(CharType ast, Object o) {
        print(indentString() + "char");
        return null;
    }

    public Object visitCharLiteral(CharLiteral ast, Object o) {
        print(indentString() + ast.spelling);
        return null;
    }

    public Object visitCharExpr(CharExpr ast, Object o) {
        print(indentString() + "CharExpr");
        ++indent;
        ast.CL.visit(this, o);
        --indent;
        return null;
    }

    public Object visitCastExpr(CastExpr ast, Object o) {
        print(indentString() + "CastExpr");
        ++indent;
        ast.tFrom.visit(this, o);
        ast.tFrom.visit(this, o);
        ++indent;
        ast.E.visit(this, o);
        --indent;
        --indent;
        return null;
    }

    @Override
    public Object visitBreakStmt(BreakStmt ast, Object o) {
        print(indentString() + "BreakStmt");
        return null;
    }

    @Override
    public Object visitContinueStmt(ContinueStmt ast, Object o) {
        print(indentString() + "ContinueStmt");
        return null;
    }

    @Override
    public Object visitReturnStmt(ReturnStmt ast, Object o) {
        print(indentString() + "ReturnStmt");
        ++indent;
        ast.E.visit(this, o);
        --indent;
        return null;
    }

    @Override
    public Object visitDeclStmt(DeclStmt ast, Object o) {
        print(indentString() + "DeclStmt");
        ast.I.visit(this, o);
        ast.E.visit(this, o);
        return null;
    }

    @Override
    public Object visitOperator(Operator ast, Object o) {
        print(indentString() + ast.spelling);
        return null;
    }

    @Override
    public Object visitBinaryExpr(BinaryExpr ast, Object o) {
        print(indentString() + "BinaryExpr");
        ++indent;
        ast.E1.visit(this, o);
        ast.O.visit(this, o);
        ast.E2.visit(this, o);
        --indent;
        return null;
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr ast, Object o) {
        print(indentString() + "UnaryExpr");
        ++indent;
        ast.O.visit(this, o);
        ast.E.visit(this, o);
        --indent;
        return null;
    }

    @Override
    public Object visitEmptyExpr(EmptyExpr ast, Object o) {
        print(indentString() + "EmptyExpr");
        return null;
    }

    @Override
    public Object visitEmptyStmt(EmptyStmt ast, Object o) {
        print(indentString() + "EmptyStmt");
        return null;
    }

    @Override
    public Object visitDeclList(DeclList ast, Object o) {
        print(indentString() + "DeclList");
        ++indent;
        ast.D.visit(this, o);
        ast.DL.visit(this, o);
        --indent;
        return null;
    }

    @Override
    public Object visitStmtList(StmtList ast, Object o) {
        print(indentString() + "StmtList");
        ++indent;
        ast.S.visit(this, o);
        ast.SL.visit(this, o);
        --indent;
        return null;
    }

    @Override
    public Object visitEmptyStmtList(EmptyStmtList ast, Object o) {
        print(indentString() + "EmptyStmtList");
        return null;
    }

    @Override
    public Object visitBooleanExpr(BooleanExpr ast, Object o) {
        print(indentString() + "BooleanExpr");
        ++indent;
        ast.BL.visit(this, o);
        --indent;
        return null;
    }

    @Override
    public Object visitBooleanLiteral(BooleanLiteral ast, Object o) {
        print(indentString() + ast.spelling);
        return null;
    }

    @Override
    public Object visitBooleanType(BooleanType ast, Object o) {
        print(indentString() + "boolean");
        return null;
    }

    @Override
    public Object visitVoidType(VoidType ast, Object o) {
        print(indentString() + "void");
        return null;
    }

    @Override
    public Object visitErrorType(ErrorType ast, Object o) {
        print(indentString() + "error");
        return null;
    }

    @Override
    public Object visitIntExpr(IntExpr ast, Object o) {
        print(indentString() + "IntExpr");
        ++indent;
        ast.IL.visit(this, o);
        --indent;
        return null;
    }

    @Override
    public Object visitIntLiteral(IntLiteral ast, Object o) {
        print(indentString() + ast.spelling);
        return null;
    }

    @Override
    public Object visitIntType(IntType ast, Object o) {
        print(indentString() + "int");
        return null;
    }

    @Override
    public Object visitArgList(Args ast, Object o) {
        print(indentString() + "ArgList");
        ++indent;
        ast.E.visit(this, o);
        ast.EL.visit(this, o);
        --indent;
        return null;
    }

    @Override
    public Object visitParaList(ParaList ast, Object o) {
        print(indentString() + "ParaList");
        ++indent;
        ast.P.visit(this, o);
        ast.PL.visit(this, o);
        --indent;
        return null;
    }

    @Override
    public Object visitEmptyParaList(EmptyParaList ast, Object o) {
        print(indentString() + "EmptyParaList");
        return null;
    }

    @Override
    public Object visitEmptyStructList(EmptyStructList ast, Object o) {
        print(indentString() + "EmptyStructList");
        return null;
    }

    @Override
    public Object visitParaDecl(ParaDecl ast, Object o) {
        print(indentString() + "ParaDecl");
        ++indent;
        ast.T.visit(this, o);
        ast.I.visit(this, o);
        --indent;
        return null;
    }

    @Override
    public Object visitEmptyCompStmt(EmptyCompStmt ast, Object o) {
        print(indentString() + "EmptyCompStmt");
        return null;
    }

    @Override
    public Object visitEmptyDeclList(EmptyDeclList ast, Object o) {
        print(indentString() + "EmptyDeclList");
        return null;
    }

    @Override
    public Object visitVarExpr(VarExpr ast, Object o) {
        print(indentString() + "VarExpr");
        ++indent;
        ast.V.visit(this, o);
        --indent;
        return null;
    }

    @Override
    public Object visitSimpleVar(SimpleVar ast, Object o) {
        print(indentString() + "SimpleVar");
        ++indent;
        ast.I.visit(this, o);
        --indent;
        return null;
    }

    @Override
    public Object visitCallExpr(CallExpr ast, Object o) {
        print(indentString() + "CallExpr");
        ++indent;
        ast.I.visit(this, o);
        ast.AL.visit(this, o);
        --indent;
        return null;
    }

    @Override
    public Object visitEmptyArgList(EmptyArgList ast, Object o) {
        print(indentString() + "EmptyArgList");
        return null;
    }

    @Override
    public Object visitLocalVarStmt(LocalVarStmt ast, Object o) {
        print(indentString() + "LocalVarStmt");
        ++indent;
        ast.V.visit(this, o);
        --indent;
        return null;
    }

    @Override
    public Object visitCallStmt(CallStmt ast, Object o) {
        print(indentString() + "CallStmt");
        ++indent;
        ast.E.visit(this, o);
        --indent;
        return null;
    }

    public Object visitLoopStmt(LoopStmt ast, Object o) {
        print(indentString() + "LoopStmt");
        ast.varName.ifPresent(var -> var.visit(this, o));
        ast.I1.ifPresent(intExpr -> intExpr.visit(this, o));
        ast.I2.ifPresent(intExpr -> intExpr.visit(this, o));
        ++indent;
        ast.S.visit(this, o);
        --indent;
        return null;
    }

    public Object visitStringExpr(StringExpr ast, Object o) {
        print(indentString() + "StringExpr");
        ++indent;
        ast.SL.visit(this, o);
        --indent;
        return null;
    }

    public Object visitEnum(Enum ast, Object o) {
        print(indentString() + "Enum" + ast.I.spelling + Arrays.toString(ast.IDs));
        return null;
    }

    @Override
    public Object visitMurkyType(MurkyType ast, Object o) {
        print(indentString() + "MurkyType");
        return null;
    }

    public Object visitEnumType(EnumType ast, Object o) {
        print(indentString() + "EnumType");
        return null;
    }

    public Object visitStructType(StructType ast, Object o) {
        print(indentString() + "StructType");
        return null;
    }

    public Object visitEmptyStructArgs(EmptyStructArgs ast, Object o) {
        print(indentString() + "EmptyStructArgs");
        return null;
    }

    public Object visitStructArgs(StructArgs ast, Object o) {
        print(indentString() + "StructArgs");
        ++indent;
        ast.E.visit(this, o);
        ast.SL.visit(this, o);
        --indent;
        return null;
    }

    public Object visitStructExpr(StructExpr ast, Object o) {
        print(indentString() + "StructExpr");
        ++indent;
        ast.I.visit(this, o);
        ast.SA.visit(this, o);
        --indent;
        return null;
    }

    public Object visitEnumExpr(EnumExpr ast, Object o) {
        print(indentString() + ast.Type.spelling + "." + ast.Entry.spelling);
        return null;
    }

    public Object visitUnknownType(UnknownType ast, Object o) {
        print(indentString() + "UnknownType");
        return null;
    }

    public Object visitStructElem(StructElem ast, Object o) {
        print(indentString() + "StructElem");
        ++indent;
        ast.T.visit(this, o);
        ast.I.visit(this, o);
        --indent;
        return null;
    }

    public Object visitStructList(StructList ast, Object o) {
        print(indentString() + "StructList");
        ++indent;
        ast.S.visit(this, o);
        ast.SL.visit(this, o);
        --indent;
        return null;
    }

    public Object visitStruct(Struct ast, Object o) {
        print(indentString() + "Struct");
        ++indent;
        ast.I.visit(this, o);
        ast.SL.visit(this, o);
        --indent;
        return null;
    }

    public Object visitStringLiteral(StringLiteral ast, Object o) {
        print(indentString() + ast.spelling);
        return null;
    }

}
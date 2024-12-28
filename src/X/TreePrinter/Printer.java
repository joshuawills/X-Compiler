package X.TreePrinter; import X.Nodes.*;
import X.Nodes.Enum;
import X.Nodes.Module;

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
        if (ast.isModuleAccess) {
            print(indentString() + ast.module.get() + "::" + ast.spelling);
        } else {
            print(indentString() + ast.spelling);
        }
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
    public Object visitTrait(Trait ast, Object o) {
        print(indentString() + "Trait");
        ++indent;
        ast.I.visit(this, o);
        ast.TL.visit(this, o);
        --indent;
        return null;
    }

    @Override 
    public Object visitMethod(Method ast, Object o) {
        print(indentString() + "Method");
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
        print(indentString() + "GlobalVar" + ast.isExported);
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

    public Object visitDecimalLiteral(DecimalLiteral ast, Object o) {
        print(indentString() + ast.spelling);
        return null;
    }

    public Object visitF32Type(F32Type ast, Object o) {
        print(indentString() + "f32");
        return null;
    }

    public Object visitF64Type(F64Type ast, Object o) {
        print(indentString() + "f64");
        return null;
    }

    public Object visitF32Expr(F32Expr ast, Object o) {
        print(indentString() + "F32Expr");
        ++indent;
        ast.DL.visit(this, o);
        --indent;
        return null;
    }

    public Object visitF64Expr(F64Expr ast, Object o) {
        print(indentString() + "F64Expr");
        ++indent;
        ast.DL.visit(this, o);
        --indent;
        return null;
    }

    public Object visitDecimalExpr(DecimalExpr ast, Object o) {
        print(indentString() + "DecimalExpr");
        ++indent;
        ast.DL.visit(this, o);
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

    public Object visitI8Type(I8Type ast, Object o) {
        print(indentString() + "i8");
        return null;
    }

    public Object visitU8Type(U8Type ast, Object o) {
        print(indentString() + "u8");
        return null;
    }

    public Object visitU32Type(U32Type ast, Object o) {
        print(indentString() + "u32");
        return null;
    }

    public Object visitU64Type(U64Type ast, Object o) {
        print(indentString() + "u64");
        return null;
    }

    public Object visitCharLiteral(CharLiteral ast, Object o) {
        print(indentString() + ast.spelling);
        return null;
    }

    public Object visitCharExpr(I8Expr ast, Object o) {
        print(indentString() + "CharExpr");
        ++indent;
        if (ast.CL.isPresent()) {
            ast.CL.get().visit(this, o);
        } else {
            ast.IL.get().visit(this, o);
        }
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
    public Object visitI64Expr(I64Expr ast, Object o) {
        print(indentString() + "I64Expr");
        ++indent;
        ast.IL.visit(this, o);
        --indent;
        return null;
    }

    @Override
    public Object visitI32Expr(I32Expr ast, Object o) {
        print(indentString() + "I32Expr");
        ++indent;
        ast.IL.visit(this, o);
        --indent;
        return null;
    }

    @Override
    public Object visitU8Expr(U8Expr ast, Object o) {
        print(indentString() + "U8Expr");
        ++indent;
        ast.IL.visit(this, o);
        --indent;
        return null;
    }

    @Override
    public Object visitU32Expr(U32Expr ast, Object o) {
        print(indentString() + "U32Expr");
        ++indent;
        ast.IL.visit(this, o);
        --indent;
        return null;
    }

    @Override
    public Object visitU64Expr(U64Expr ast, Object o) {
        print(indentString() + "U64Expr");
        ++indent;
        ast.IL.visit(this, o);
        --indent;
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
    public Object visitI64Type(I64Type ast, Object o) {
        print(indentString() + "i64");
        return null;
    }

    @Override 
    public Object visitVariaticType(VariaticType ast, Object o) {
        print(indentString() + "...");
        return null;
    }

    @Override
    public Object visitI32Type(I32Type ast, Object o) {
        print(indentString() + "i32");
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
        if (ast.isLibC) {
            print(indentString() + "@CallExpr");
        } else {
            print(indentString() + "CallExpr");
        }
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
    public Object visitTupleDestructureAssignStmt(TupleDestructureAssignStmt ast, Object o) {
        print(indentString() + "TupleDestructureAssignStmt");
        ++indent;
        ast.TDA.visit(this, o);
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
        print(indentString() + "Enum" + ast.I.spelling + Arrays.toString(ast.IDs) + ast.isExported);
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

    public Object visitAssignmentExpr(AssignmentExpr ast, Object o) {
        print(indentString() + "AssignmentExpr");
        ++indent;
        ast.LHS.visit(this, o);
        ast.O.visit(this, o);
        ast.RHS.visit(this, o);
        --indent;
        return null;
    }

    public Object visitExprStmt(ExprStmt ast, Object o) {
        print(indentString() + "ExprStmt");
        ++indent;
        ast.E.visit(this, o);
        --indent;
        return null;
    }

    public Object visitDerefExpr(DerefExpr ast, Object o) {
        print(indentString() +"DerefExpr");
        ++indent;
        ast.E.visit(this, o);
        --indent;
        return null;
    }

    public Object visitEnumExpr(EnumExpr ast, Object o) {
        print(indentString() + ast.Type.spelling + "." + ast.Entry.spelling);
        return null;
    }

    public Object visitEmptyStructAccessList(EmptyStructAccessList ast, Object o) {
        return null;
    }

    public Object visitStructAccessList(StructAccessList ast, Object o) {
        ast.SA.visit(this, o);
        ast.SAL.visit(this, o);
        return null;
    }

    public Object visitStructAccess(StructAccess ast, Object o) {
        print(indentString() + ast.varName.spelling);
        ast.L.visit(this, o);
        return null;
    }

    public Object visitDotExpr(DotExpr ast, Object o) {
        print(indentString() + "DotExpr: ");
        ast.IE.visit(this, o);
        ++indent;
        ast.E.visit(this, o);
        --indent;
        return null;
    }

    public Object visitMethodAccessExpr(MethodAccessExpr ast, Object o) {
        print(indentString() + "MethodAccessExpr");
        ++indent;
        ast.I.visit(this, o);
        ast.args.visit(this, o);
        ast.next.visit(this, o);
        --indent;
        return null;
    }

    public Object visitMethodAccessWrapper(MethodAccessWrapper ast, Object o) {
        print(indentString() + "MethodAccessWrapper");
        ++indent;
        ast.methodAccessExpr.visit(this, o);
        --indent;
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
        print(indentString() + "Struct " + ast.isExported);
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

    public Object visitSizeOfExpr(SizeOfExpr ast, Object o) {
        print(indentString() + "SizeOf");
        ++indent;
        if (ast.typeV.isPresent()) {
            ast.typeV.get().visit(this, o);
        } else {
            ast.varExpr.get().visit(this, o);
        }
        --indent;
        return null;
    }

    public Object visitTypeOfExpr(TypeOfExpr ast, Object o) {
        print(indentString() + "TypeOf");
        ++indent;
        ast.E.visit(this, o);
        --indent;
        return null;
    }

    public Object visitModule(Module ast, Object o) {
        return null;
    }

    public Object visitImportStmt(ImportStmt ast, Object o) {
        print(indentString() + "ImportStmt");
        return null;
    }

    public Object visitUsingStmt(UsingStmt ast, Object o) {
        print(indentString() + "UsingStmt");
        return null;
    }

    public Object visitNullExpr(NullExpr ast, Object o) {
        print(indentString() + "null");
        return null;
    }

    public Object visitEmptyTypeList(EmptyTypeList ast, Object o) {
        print(indentString() + "EmptyTypeList");
        return null;
    }

    public Object visitTypeList(TypeList ast, Object o) {
        print(indentString() + "TypeList");
        ++indent;
        ast.T.visit(this, o);
        ast.TL.visit(this, o);
        --indent;
        return null;
    }

    public Object visitTupleType(TupleType ast, Object o) {
        print(indentString() + "TupleType");
        ++indent;
        ast.TL.visit(this, o);
        --indent;
        return null;
    }

    public Object visitTupleExpr(TupleExpr ast, Object o) {
        print(indentString() + "TupleExpr");
        ++indent;
        ast.EL.visit(this, o);
        --indent;
        return null;
    }

    public Object visitTupleExprList(TupleExprList ast, Object o) {
        print(indentString() + "TupleExprList");
        ++indent;
        ast.E.visit(this, o);
        ast.EL.visit(this, o);
        --indent;
        return null;
    }

    public Object visitEmptyTupleExprList(EmptyTupleExprList ast, Object o) {
        print(indentString() + "EmptyTupleExprList");
        return null;
    }

    public Object visitTupleAccess(TupleAccess ast, Object o) {
        print(indentString() + "TupleAccess");
        ++indent;
        ast.I.visit(this, o);
        ast.index.visit(this, o);
        --indent;
        return null;
    }

    public Object visitTupleDestructureAssign(TupleDestructureAssign ast, Object o) {
        print(indentString() + "TupleDestructureAssign");
        ++indent;
        ast.idents.visit(this, o);
        ast.E.visit(this, o);
        --indent;
        return null;
    }

    public Object visitIdentsList(IdentsList ast, Object o) {
        print(indentString() + "IdentsList");
        ++indent;
        ast.I.visit(this, o);
        ast.IL.visit(this, o);
        --indent;
        return null;
    }

    public Object visitEmptyIdentsList(EmptyIdentsList ast, Object o) {
        print(indentString() + "EmptyIdentsList");
        return null;
    }

    public Object visitEmptyTraitList(EmptyTraitList ast, Object o) {
        print(indentString() + "EmptyTraitList");
        return null;
    }

    public Object visitTraitList(TraitList ast, Object o) {
        print(indentString() + "TraitList");
        ++indent;
        ast.TF.visit(this, o);
        ast.L.visit(this, o);
        --indent;
        return null;
    }

    public Object visitImpl(Impl ast, Object o) {
        print(indentString() + "Impl");
        ast.trait.visit(this, o);
        ast.struct.visit(this, o);
        ++indent;
        ast.IL.visit(this, o);
        --indent;
        return null;
    }

    public Object visitMethodList(MethodList ast, Object o) {
        print(indentString() + "MethodList");
        ++indent;
        ast.M.visit(this, o);
        ast.L.visit(this, o);
        --indent;
        return null;
    }

    public Object visitEmptyMethodList(EmptyMethodList ast, Object o) {
        print(indentString() + "EmptyMethodList");
        return null;
    }

    public Object visitExtern(Extern ast, Object o) {
        print(indentString() + "Extern");
        ++indent;
        if (ast.F != null) {
            ast.F.visit(this, o);
        } else {
            ast.G.visit(this, o);
        }
        --indent;
        return null;
    }

}
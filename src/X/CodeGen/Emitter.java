package X.CodeGen;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Stack;

import X.AllModules;
import X.Evaluator.Evaluator;
import X.Lexer.Position;
import X.Nodes.*;
import X.Nodes.Enum;
import X.Nodes.Module;

public class Emitter implements Visitor {

    private final String outputName;
    private ArrayType arrayDetails;
    private String arrName = "";
    private final Position dummyPos = new Position();

    public boolean inCallExpr = false;

    String formattedCurrentPath;
    boolean inMainModule = true;

    public Emitter(String outputName) {
        this.outputName = outputName;
    }

    public AllModules modules;
    public Module currentModule;

    public final void gen() {

        modules = AllModules.getInstance();
        ArrayList<Module> mainModule = modules.getModules();

        emitN("declare i32 @printf(i8*, ...)");
        emitN("declare i32 @scanf(i8*, ...)");
        emitN("declare ptr @malloc(i64)");
        emitN("declare void @free(ptr)");
        emitN("@.Istr = constant [4 x i8] c\"%d\\0A\\00\"");
        emitN("@.Cstr = constant [4 x i8] c\"%c\\0A\\00\"");
        emitN("@.IFstr = constant [6 x i8] c\"%.2f\\0A\\00\"");
        emitN("@.IDstr = constant [6 x i8] c\"%.2f\\0A\\00\"");
        emitN("@.IIstr = private unnamed_addr constant [3 x i8] c\"%d\\00\"");       

        // Visiting all the structs
        for (Module m: mainModule) {
            formattedCurrentPath = m.fileName.replace("/", ".");
            for (Struct s: m.getStructs().values()) {
                s.visit(this, null);
            }
        }

        // Visiting all the global vars
        for (Module m: mainModule) {
            formattedCurrentPath = m.fileName.replace("/", ".");
            for (GlobalVar v: m.getVars().values()) {
                v.visit(this, null);
            }
        }
        
        // Visiting all the functions
        inMainModule = true;
        for (Module m: mainModule) {
            currentModule = m;
            formattedCurrentPath = m.fileName.replace("/", ".");
            for (Function f: m.getFunctionsBarStandard().values()) {
                f.visit(this, null);
            }
            inMainModule = false;
        }

        LLVM.dump(outputName);
    }

    public Object visitFunction(Function ast, Object o) {
        if (!ast.isUsed && !ast.I.spelling.equals("main")) {
            return null;
        }
        Frame f = new Frame(ast.I.spelling.equals("main"));
        emit("define ");
        ast.T.visit(this, f);
        if (inMainModule) {
            emit(" @" + ast.I.spelling);
        } else {
            emit(" @" + formattedCurrentPath + ast.I.spelling);
        }
        if (!ast.I.spelling.equals("main")) {
            emit("." + ast.TypeDef);
        }
        ast.PL.visit(this, f);
        emitN(" {");

        // Bind mutable variables to a local variable
        List PL = ast.PL;
        while (!PL.isEmptyParaList()) {
            ParaDecl P = ((ParaList) PL).P;
            Type t = P.T;
            if ((P.isMut && !t.isArray()) || t.isPointer()) {
                emit("\t%" + P.I.spelling + "0 = alloca ");
                t.visit(this, o);
                emit("\n\tstore ");
                t.visit(this, o);
                emit(" %" + P.I.spelling + ", ");
                t.visit(this, o);
                emitN("* %" + P.I.spelling + "0");
            }
            PL = ((ParaList) PL).PL;
        }

        ast.S.visit(this, f);
        if (ast.I.spelling.equals("main")) {
           emitN("\tret i64 0");
        }

        if (!ast.S.containsExit && ast.T.isVoid()) {
            emitN("\t ret void");
        }

        emitN("}");
        return null;
    }

    // TODO: make this handle empty expressions (should be easy)
    public Object visitGlobalVar(GlobalVar ast, Object o) {
        Object result;
        if (ast.T.isArray()) {
            Type T = ((ArrayType) ast.T).t;
            result = Evaluator.evalExpression(ast.E, T);
        } else {
            result = Evaluator.evalExpression(ast.E);
        }
        emit("@" + ast.I.spelling + " = global ");
        ast.T.visit(this, o);
        emitN(" " + result.toString());
        return null;
    }

    public Object visitCompoundStmt(CompoundStmt ast, Object o) {
        ast.SL.visit(this, o);
        return null;
    }

    public Object visitLocalVar(LocalVar ast, Object o) {
        String depth = ast.index;
        Frame f = (Frame) o;
        emit("\t%" + ast.I.spelling + depth + " = alloca ");
        ast.T.visit(this, o);
        emitN("");

        String name = ast.I.spelling + depth;
        if (ast.T.isArray()) {
            arrName = name;
            arrayDetails = (ArrayType) ast.T;

            handleBitCast(ast.T, name, f);

            Expr E = ast.E;
            if (E.isEmptyExpr()) {
                E = new ArrayInitExpr(new EmptyArgList(dummyPos), dummyPos);
            }
            E.visit(this, o);
            return null;
        } else if (ast.T.isStruct()) {
            if (ast.E.isEmptyExpr()) {
                return null;
            }

            handleBitCast(ast.T, ast.I.spelling + depth, f);

            ast.E.visit(this, o);

            if (!ast.E.isStructExpr()) {
                int srcIndex = ast.E.tempIndex;

                int v3 = f.getNewIndex();
                int v4 = f.getNewIndex();

                emit("\t%" + v3 + " = getelementptr ");
                ast.T.visit(this, o);
                emit(", ");
                ast.T.visit(this, o);
                emitN("* null, i32 1");
                emit("\t%" + v4 + " = ptrtoint ");
                ast.T.visit(this, o);
                emitN("* %" + v3 + " to i64");
                
                emitN("\tcall void @llvm.memcpy.p0.p0.i64(ptr %" + ast.I.spelling + depth + ", ptr %" + srcIndex
                + ", i64 %" + v4 + ", i1 false)");

            }
            return null;
        }

        if (ast.E.isEmptyExpr()) {
            // TODO: assign default values
            return null;
        }

        ast.E.visit(this, o);
        emit("\tstore ");
        ast.T.visit(this, o);
        int value = f.localVarIndex - 1;
        emit(" %" + value + ", ");
        ast.T.visit(this, o);
        emitN("* %" + ast.I.spelling + depth);
        return null;
    }

    private String trueBottom = "";

    public Object visitIfStmt(IfStmt ast, Object o) {

        Frame f = (Frame) o;
        ast.E.visit(this, o);
        String middle= f.getNewLabel();
        String elseC = f.getNewLabel();
        String bottom = f.getNewLabel();
        trueBottom = bottom;
        int index = ast.E.tempIndex;
        emitN("\tbr i1 %" + index + ", label %" + middle + ", label %" + elseC);
        emitN("\n" + middle + ":");
        ast.S1.visit(this, o);
        if (!ast.S1.containsExit) {
            emitN("\tbr label %" + bottom);
        }

        emitN("\n" + elseC + ":");
        ast.S2.visit(this, o);
        ast.S3.visit(this, o);
        if (!ast.S3.containsExit) {
            emitN("\tbr label %" + bottom);
        }

        emitN("\n" + bottom + ":");
        trueBottom = "";
        return null;
    }

    public Object visitElseIfStmt(ElseIfStmt ast, Object o) {

        Frame f = (Frame) o;
        String middle= f.getNewLabel();
        String nextCondition = f.getNewLabel();
        ast.E.visit(this, o);
        int index = ast.E.tempIndex;
        emitN("\tbr i1 %" + index + ", label %" + middle + ", label %" + nextCondition);
        emitN("\n" + middle + ":");
        ast.S1.visit(this, o);
        if (!ast.S1.containsExit) {
            emitN("\tbr label %" + trueBottom);
        }

        emitN(nextCondition + ":");
        ast.S2.visit(this, o);
        return null;
    }

    public Object visitForStmt(ForStmt ast, Object o) {
        Frame f = (Frame) o;

        String top = f.getNewLabel();
        String middle = f.getNewLabel();
        String m2 = f.getNewLabel();
        String bottom = f.getNewLabel();

        f.brkStack.push(bottom);
        f.conStack.push(m2);

        ast.S1.visit(this, o);

        emitN("\tbr label %" + top);
        emitN("\n" + top + ":");
        ast.E2.visit(this, o);
        if (!ast.E2.isEmptyExpr()) {
            int index = ast.E2.tempIndex;
            emitN("\tbr i1 %" + index + ", label %" + middle + ", label %" + bottom);
        } else {
            emitN("\tbr label %" + middle);
        }
        emitN("\n" + middle + ":");
        ast.S.visit(this, o);
        if (!ast.S.containsExit) {
            emitN("\tbr label %" + m2);
        }
        emitN("\n" + m2 + ":");
        ast.S3.visit(this, o);
        if (!ast.S.containsExit) {
            emitN("\tbr label %" + top);
        }
        emitN("\n" + bottom+ ":");

        f.brkStack.pop();
        f.conStack.pop();
        return null;
    }

    public Object visitWhileStmt(WhileStmt ast, Object o) {
        Frame f = (Frame) o;
        String top = f.getNewLabel();
        String middle= f.getNewLabel();
        String bottom = f.getNewLabel();

        f.brkStack.push(bottom);
        f.conStack.push(top);

        emitN("\tbr label %" + top);
        emitN("\n" + top + ":");
        ast.E.visit(this, o);
        int index = ast.E.tempIndex;
        emitN("\tbr i1 %" + index + ", label %" + middle + ", label %" + bottom);
        emitN("\n" + middle + ":");
        ast.S.visit(this, o);
        if (!ast.S.containsExit) {
            emitN("\tbr label %" + top);
        }
        emitN("\n" + bottom+ ":");

        f.brkStack.pop();
        f.conStack.pop();

        return null;
    }

    public Object visitBreakStmt(BreakStmt ast, Object o) {
        Frame f = (Frame) o;
        String label = f.brkStack.peek();
        emitN("\tbr label %" + label);
        return null;
    }

    public Object visitContinueStmt(ContinueStmt ast, Object o) {
        Frame f = (Frame) o;
        String label = f.conStack.peek();
        emitN("\tbr label %" + label);
        return null;
    }

    public Object visitReturnStmt(ReturnStmt ast, Object o) {
        Frame f = (Frame) o;
        if (ast.E.type.isVoid()) {
            emitN("    ret void");
            return null;
        }
        ast.E.visit(this, o);
        emit("\tret ");
        int index = f.localVarIndex - 1;
        ast.E.type.visit(this, o);
        emitN(" %" + index);
        return null;
    }

    public Object visitOperator(Operator ast, Object o) {
        Frame f = (Frame) o;
        switch (ast.spelling) {
            case "i8-", "i64-", "f32-" -> {
                if (ast.parent instanceof BinaryExpr parent) {
                    int numOne = parent.E1.tempIndex;
                    int numTwo = parent.E2.tempIndex;
                    int newNum = f.getNewIndex();
                    parent.tempIndex = newNum;
                    if (ast.spelling.equals("c-")) {
                        emitN("\t%" + newNum + " = sub i8 %" + numOne + ", %" + numTwo);
                    } else {
                        emitN("\t%" + newNum + " = sub i64 %" + numOne + ", %" + numTwo);
                    }
                } else if (ast.parent instanceof UnaryExpr parent) {
                    int numOne = parent.E.tempIndex;
                    int newNum = f.getNewIndex();
                    parent.tempIndex = newNum;
                    if (ast.spelling.equals("c-")) {
                        emitN("\t%" + newNum + " = sub i8 0, %" + numOne);
                    } else {
                        emitN("\t%" + newNum + " = sub i64 0, %" + numOne);
                    }
                }
            }
            case "b!" -> {
                if (ast.parent instanceof UnaryExpr parent) {
                    int numOne = parent.E.tempIndex;
                    int newNum = f.getNewIndex();
                    parent.tempIndex = newNum;
                    emitN("\t%" + newNum + " = " +  "xor i1 1, " + " %" + numOne);
                }
            }
            default -> {
                if (ast.parent instanceof BinaryExpr parent) {
                    int numOne = parent.E1.tempIndex;
                    int numTwo = parent.E2.tempIndex;
                    int newNum = f.getNewIndex();
                    parent.tempIndex = newNum;
                    emitN("\t%" + newNum + " = " +  opToCommand(ast.spelling) +
                        " %" + numOne + ", %" + numTwo);
                }
            }
        }
        return null;
    }

    public String opToCommand(String input) {
        return switch (input)  {
            case "i64+" ->  "add i64";
            case "i8+" ->  "add i8";
            case "f32+" ->  "fadd float";
            case "f64+" ->  "fadd double";
            case "i64*" -> "mul i64";
            case "i8*" -> "mul i8";
            case "f32*" -> "fmul float";
            case "f64*" -> "fmul double";
            case "i64%", "ii%" -> "srem i64";
            case "i8%" -> "srem i8";
            case "i64/" -> "sdiv i64";
            case "i8/" -> "sdiv i8";
            case "f32/" -> "fdiv float";
            case "f64/" -> "fdiv double";
            case "i64==" -> "icmp eq i64";
            case "i8==" -> "icmp eq i8";
            case "f32==" -> "fcmp oeq float";
            case "f64==" -> "fcmp oeq double";
            case "b==" -> "icmp eq i1";
            case "i64!=" -> "icmp ne i64";
            case "i8!=" -> "icmp ne i8";
            case "f32!=" -> "fcmp one float";
            case "f64!=" -> "fcmp one double";
            case "b!=" -> "icmp ne i1";
            case "i64<=" -> "icmp sle i64";
            case "i8<=" -> "icmp sle i8";
            case "f32<=" -> "fcmp ole float";
            case "f64<=" -> "fcmp ole double";
            case "i64<" -> "icmp slt i64";
            case "i8<" -> "icmp slt i8";
            case "f32<" -> "fcmp olt float";
            case "f64<" -> "fcmp olt double";
            case "i64>" -> "icmp sgt i64";
            case "i8>" -> "icmp sgt i8";
            case "f32>" -> "fcmp ogt float";
            case "f64>" -> "fcmp ogt double";
            case "i64>=" -> "icmp sge i64";
            case "i8>=" -> "icmp sge i8";
            case "f32>=" -> "fcmp oge float";
            case "f64>=" -> "fcmp oge double";
            default -> {
                System.out.println("opToCommand not implemented: " + input);
                yield "";
            }
        };
    }

    public Object visitBinaryExpr(BinaryExpr ast, Object o) {
        Frame f = (Frame) o;
        if (ast.O.spelling.equals("b&&")) {
            String X = f.getPreceding();
            String L1 = f.getNewLabel();
            String L2 = f.getNewLabel();
            ast.E1.visit(this, o);
            int indexOne = ast.E1.tempIndex;
            emitN("\tbr i1 %" + indexOne + ", label %" + L1 + ", label %" + L2);
            emitN(L1 +":");
            ast.E2.visit(this, o);
            emitN("\tbr label %" + L2);
            int indexTwo = ast.E2.tempIndex;
            emitN(L2 +":");
            int i = f.getNewIndex();
            ast.tempIndex = i;
            emitN("\t%" + i + " = phi i1 [ false, %" + X + " ], [ %" + indexTwo + ", %" + L1 + " ]");
            return null;
        }

        if (ast.O.spelling.equals("b||")) {
            String X = f.getPreceding();
            String L1 = f.getNewLabel();
            String L2 = f.getNewLabel();
            ast.E1.visit(this, o);
            int indexOne = ast.E1.tempIndex;
            emitN("\tbr i1 %" + indexOne + ", label %" + L2 + ", label %" + L1);
            emitN(L1 +":");
            ast.E2.visit(this, o);
            emitN("\tbr label %" + L2);
            int indexTwo = ast.E2.tempIndex;
            emitN(L2 +":");
            int i = f.getNewIndex();
            ast.tempIndex = i;
            emitN("\t%" + i + " = phi i1 [ true, %" + X + " ], [ %" + indexTwo + ", %" + L1 + " ]");
            return null;
        }

        ast.E1.visit(this, o);
        ast.E2.visit(this, o);
        ast.O.visit(this, o);
        return null;
    }

    public Object visitUnaryExpr(UnaryExpr ast, Object o) {
        Frame f = (Frame) o;
        if (ast.O.spelling.equals("&")) {
            VarExpr VE = (VarExpr) ast.E;
            SimpleVar SV = (SimpleVar) VE.V;
            Decl d = (Decl) SV.I.decl;
            int index = handleBitCast(d.T, SV.I.spelling + d.index, f);
            ast.tempIndex = index;
            return null;
        }
        ast.E.visit(this, o);
        ast.O.visit(this, o);
        return null;
    }

    public Object visitStmtList(StmtList ast, Object o) {
        List SL = ast;
        while (true) {
            StmtList stmtList = (StmtList) SL;
            stmtList.S.visit(this, o);
            if (stmtList.SL.isEmptyStmtList()) {
                break;
            } else if (stmtList.S.isReturnStmt()) {
                break;
            } else {
                SL = stmtList.SL;
            }
        }
        return null;
    }

    public Object visitBooleanExpr(BooleanExpr ast, Object o) {
        Frame f = (Frame) o;
        int value = ast.BL.spelling.equals("true") ? 1 : 0;
        int num = f.getNewIndex();
        emitN("\t%" + num + " = add i1 0, " + value);
        ast.tempIndex = num;
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

    public Object visitI64Expr(I64Expr ast, Object o) {
        Frame f = (Frame) o;
        String value = ast.IL.spelling;
        int num = f.getNewIndex();
        emitN("\t%" + num + " = add i64 0, " + value);
        ast.tempIndex = num;
        return null;
    }

   public Object visitI32Expr(I32Expr ast, Object o) {
        Frame f = (Frame) o;
        String value = ast.IL.spelling;
        int num = f.getNewIndex();
        emitN("\t%" + num + " = add i32 0, " + value);
        ast.tempIndex = num;
        return null;
    }

    public Object visitI64Type(I64Type ast, Object o) {
        emit(LLVM.I64_TYPE);
        return null;
    }
    
    public Object visitI32Type(I32Type ast, Object o) {
        emit(LLVM.I32_TYPE);
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

    public Object visitEmptyParaList(EmptyParaList ast, Object o) {
        emit("()");
        return null;
    }

    public Object visitParaDecl(ParaDecl ast, Object o) {
        if (ast.T.isArray()) {
            PointerType pT = new PointerType(dummyPos, ((ArrayType) ast.T).t);
            pT.visit(this, o);
            emit(" %" + ast.I.spelling);
            return null;
        } else {
            ast.T.visit(this, o);
        }

        if (!ast.isMut && !ast.T.isPointer()) {
            emit(" %" + ast.I.spelling + "0");
        } else {
            emit(" %" + ast.I.spelling);
        }
        return null;
    }

    public Object visitVarExpr(VarExpr ast, Object o) {
        ast.V.visit(this, o);
        ast.tempIndex = ((Frame) o).localVarIndex - 1;
        return null;
    }

    public Object visitSimpleVar(SimpleVar ast, Object o) {
        Frame f = (Frame) o;

        if (ast.I.spelling.equals("$")) {
            int newIndex = f.getNewIndex();
            emitN("\t%" + newIndex + " = load i64, i64* %" + "$".repeat(f.getDollarDepth()));
            return null;
        }

        AST d = ast.I.decl;
        if (d.isLocalVar()) {
            LocalVar l = (LocalVar) d;
            int newIndex = -1;
            if (l.T.isArray()) {
                newIndex = f.getNewIndex();
                ArrayType t = (ArrayType) l.T;
                emit("\t%" + newIndex + " = getelementptr inbounds ");
                int length = t.length;
                l.T.visit(this, length);
                emit(", ");
                l.T.visit(this, length);
                emitN("* %" + ast.I.spelling + l.index + ", i32 0, i32 0");
                return null;
            } else if (l.T.isStruct()) {

                newIndex = handleBitCast(l.T, ast.I.spelling + l.index, f);
                if (!ast.inDeclaringLocalVar) {
                    int v2 = f.getNewIndex();
                    handleLoad(l.T, newIndex, v2, f);
                }

                return null;
            } else {
                newIndex = f.getNewIndex();
            }

            handleLoad(l.T, ast.I.spelling + l.index, newIndex, f);
        } else if (d.isParaDecl()) {
            ParaDecl p = (ParaDecl) d;

            if (p.T.isStruct()) {
                int newIndex = handleBitCast(p.T, ast.I.spelling + 0, f);

                if (!ast.inDeclaringLocalVar) {
                    int v2 = f.getNewIndex();
                    handleLoad(p.T, newIndex, v2, f);
                }

                return null;
            }

            if (p.isMut) {
                int newIndex = f.getNewIndex();
                handleLoad(p.T, ast.I.spelling + p.index, newIndex, f);
                return null;
            }

            int newIndex = f.getNewIndex();
            emit("\t%" + newIndex + " = ");
            Type T = p.T;
            if (T.isI64() || T.isBoolean() || T.isI8() || T.isEnum()) {
                emit("add ");
                T.visit(this, o);
                emit(" 0, ");
            }
            emitN(" %" + ast.I.spelling + "0");
        } else if (d.isGlobalVar()) {
            GlobalVar g = (GlobalVar) d;
            int newIndex = f.getNewIndex();
            emit("\t%" + newIndex + " = load ");
            g.T.visit(this, o);
            emit(", ");
            g.T.visit(this, o);
            emitN("* @" + ast.I.spelling);
        }
        return null;
   }

    public void handleOutI64(CallExpr ast, Object o) {
        Frame f = (Frame) o;
        ((Args) ast.AL).E.visit(this, o);
        int index = ((Args) ast.AL).E.tempIndex;
        int indexStr = f.getNewIndex();
        int newIndex = f.getNewIndex();
        emitN("\t%" + indexStr + " = getelementptr [4 x i8], [4 x i8]* @.Istr, i32 0, i32 0");
        emitN("\t%" + newIndex + " = call i32 (i8*, ...) @printf(i8* %" + indexStr + ", i64 %" + index + ")");
    }

    public void handleOutChar(CallExpr ast, Object o) {
        Frame f = (Frame) o;
        ((Args) ast.AL).E.visit(this, o);
        int index = ((Args) ast.AL).E.tempIndex;
        int indexStr = f.getNewIndex();
        int newIndex = f.getNewIndex();
        emitN("\t%" + indexStr + " = getelementptr [4 x i8], [4 x i8]* @.Cstr, i32 0, i32 0");
        emitN("\t%" + newIndex + " = call i32 (i8*, ...) @printf(i8* %" + indexStr + ", i8 %" + index + ")");
    }

    public void handleOutF32(CallExpr ast, Object o) {
        Frame f = (Frame) o;
        ((Args) ast.AL).E.visit(this, o);
        int index = ((Args) ast.AL).E.tempIndex;
        int valIndex = f.getNewIndex();
        emitN("\t%" +valIndex + " = fpext float %" + index + " to double");
        int indexStr = f.getNewIndex();
        int newIndex = f.getNewIndex();
        emitN("\t%" + indexStr + " = getelementptr [6 x i8], [6 x i8]* @.IFstr, i32 0, i32 0");
        emitN("\t%" + newIndex + " = call i32 (i8*, ...) @printf(i8* %" + indexStr + ", double %" + valIndex + ")");
    }

    public void handleOutF64(CallExpr ast, Object o) {
        Frame f = (Frame) o;
        ((Args) ast.AL).E.visit(this, o);
        int index = ((Args) ast.AL).E.tempIndex;
        int indexStr = f.getNewIndex();
        int newIndex = f.getNewIndex();
        emitN("\t%" + indexStr + " = getelementptr [6 x i8], [6 x i8]* @.IDstr, i32 0, i32 0");
        emitN("\t%" + newIndex + " = call i32 (i8*, ...) @printf(i8* %" + indexStr + ", double %" + index + ")");
    }

    public void handleOutStr(CallExpr ast, Object o) {
        Frame f = (Frame) o;
        Expr arg = ((Args) ast.AL).E;
        arg.visit(this, o);
        int index = arg.tempIndex;
        emitN("\tcall i32 (i8*, ...) @printf(i8* %" + index + ")");
        f.getNewIndex();
    }

    public void handleMalloc(CallExpr ast, Object o) {
        Frame f = (Frame) o;
        ((Args) ast.AL).E.visit(this, o);
        int index = ((Args) ast.AL).E.tempIndex;
        int indexStr = f.getNewIndex();
        emitN("\t%" + indexStr + " = call i8* @malloc(i64 %" + index + ")");
        ast.tempIndex = indexStr;
    }

    public void handleFree(CallExpr ast, Object o) {
        ((Args) ast.AL).E.visit(this, o);
        int index = ((Args) ast.AL).E.tempIndex;
        emitN("\tcall void @free(ptr %" + index + ")");
    }

    public Object visitCallExpr(CallExpr ast, Object o) {

        if (ast.I.spelling.equals("malloc")) {
            handleMalloc(ast, o);
            return null;
        }

        if (ast.I.spelling.equals("free")) {
            handleFree(ast, o);
            return null;
        }

        if (ast.I.spelling.equals("outI64")) {
            handleOutI64(ast, o);
            return null;
        }

        if (ast.I.spelling.equals("outChar")) {
            handleOutChar(ast, o);
            return null;
        }

        if (ast.I.spelling.equals("outStr")) {
            handleOutStr(ast, o);
            return null;
        }

        if (ast.I.spelling.equals("outF32")) {
            handleOutF32(ast, o);
            return null;
        }

        if (ast.I.spelling.equals("outF64")) {
            handleOutF64(ast, o);
            return null;
        }

        Frame f = (Frame) o;

        // Evaluate all the expressions
        inCallExpr = true;
        if (!ast.AL.isEmptyArgList()) {
            Args AL = (Args) ast.AL;
            while (true) {
                AL.E.visit(this, o);
                if (AL.EL.isEmptyArgList()) {
                    break;
                }
                AL = (Args) AL.EL;
            }
        }
        inCallExpr = false;

        Function functionRef = (Function) ast.I.decl;
        if (functionRef.T.isVoid()) {
            emit("\tcall ");
        } else {
            int num = f.getNewIndex();
            ast.tempIndex = num;
            emit("\t%" + num + " = call ");
        }
        functionRef.T.visit(this, o);
        if (ast.I.isModuleAccess) {
            Module refMod = currentModule.getModuleFromAlias(ast.I.module.get());
            String path = refMod.fileName.replace("/", ".");
            emit(" @" + path + ast.I.spelling + "." + ast.TypeDef);
        } else {
            emit(" @" + ast.I.spelling + "." + ast.TypeDef);
        }

        emit("(");
        if (!ast.AL.isEmptyArgList()) {
            Args AL = (Args) ast.AL;
            while (true) {
                Expr E = AL.E;
                int index = E.tempIndex;
                // pass arrays by ref, not val
                if (E.type.isArray()) {
                    if (E.isArrayIndexExpr()) {
                        Type T = ((ArrayType) E.type).t;
                        T.visit(this, o);
                    } else {
                        PointerType pT = new PointerType(dummyPos, ((ArrayType) E.type).t);
                        pT.visit(this, o);
                    }
                } else {
                    E.type.visit(this, o);
                }
                emit(" %" + index);
                if (AL.EL.isEmptyArgList()) {
                    break;
                } else {
                    emit(", ");
                }
                AL = (Args) AL.EL;
            }
        }
        emitN(")");
        return null;
    }

    public Object visitEmptyArgList(EmptyArgList ast, Object o) {
        emit("()");
        return null;
    }

    public Object visitLocalVarStmt(LocalVarStmt ast, Object o) {
        ast.V.visit(this, o);
        return null;
    }

    public Object visitLoopStmt(LoopStmt ast, Object o) {
        Frame f = (Frame) o;
        int newIndex;

        String dollars;
        dollars = ast.varName.map(lv -> lv.I.spelling + lv.index).orElseGet(() -> "$".repeat(f.getDollarDepth() + 1));
        f.setDollarDepth(f.getDollarDepth() + 1);

        String topLabel = f.getNewLabel();
        String midLabel = f.getNewLabel();
        String iterateLabel = f.getNewLabel();
        String bottomLabel = f.getNewLabel();
        f.conStack.push(iterateLabel);
        f.brkStack.push(bottomLabel);

        if (ast.I1.isPresent()) {
            // bounded on upper end
            emitN("\t%" + dollars + " = alloca i64");
            if (ast.I2.isPresent()) {
                ast.I1.get().visit(this, o);
                int lowerIndex = ast.I1.get().tempIndex;
                newIndex = f.getNewIndex();
                emitN("\t%" + newIndex + " = add i64 0, %" + lowerIndex);
            } else {
                newIndex = f.getNewIndex();
                emitN("\t%" + newIndex + " = add i64 0, 0");
            }
            emitN("\tstore i64 %" + newIndex + ", i64* %" + dollars);
            emitN("\tbr label %" + topLabel);
            emitN(topLabel + ":");

            int tempIndex;
            if (ast.I2.isPresent()) {
                ast.I2.get().visit(this, o);
                tempIndex = ast.I2.get().tempIndex;
            } else {
                ast.I1.get().visit(this, o);
                tempIndex = ast.I1.get().tempIndex;
            }
            int dolIndex = f.getNewIndex();
            emitN("\t%" + dolIndex + " = load i64, i64* %" + dollars);
            int boolIndex = f.getNewIndex();
            emitN("\t%" + boolIndex + " = icmp sge i64 %" + dolIndex + ", %" + tempIndex);
            emitN("\tbr i1 %" + boolIndex + ", label %" + bottomLabel + ", label %" + midLabel);
            emitN(midLabel + ":");

            ast.S.visit(this, o);
            int dol2Index = f.getNewIndex();
            int newIndexTwo = f.getNewIndex();

            emitN("\tbr label %" + iterateLabel);
            emitN(iterateLabel + ":");
            emitN("\t%" + dol2Index + " = load i64, i64* %" + dollars);
            emitN("\t%" + newIndexTwo + " = add i64 1, %" + dol2Index);
            emitN("\tstore i64 %" + newIndexTwo +", i64* %" + dollars);
            emitN("\tbr label %" + topLabel);
            emitN(bottomLabel + ":");
        } else {
            // no bounds
            newIndex = f.getNewIndex();
            emitN("\t%" + dollars + " = alloca i64");
            emitN("\t%" + newIndex + " = add i64 0, 0");
            emitN("\tstore i64 %" + newIndex + ", i64* %" + dollars);
            emitN("\tbr label %" + topLabel);
            emitN(topLabel + ":");
            ast.S.visit(this, o);
            int dol2Index = f.getNewIndex();

            emitN("\tbr label %" + iterateLabel);
            emitN(iterateLabel + ":");
            emitN("\t%" + dol2Index + " = load i64, i64* %" + dollars);
            int newIndexTwo = f.getNewIndex();
            emitN("\t%" + newIndexTwo + " = add i64 1, %" + dol2Index);
            emitN("\tstore i64 %" + newIndexTwo +", i64* %" + dollars);
            emitN("\tbr label %" + topLabel);
            emitN(bottomLabel + ":");
        }
        f.conStack.pop();
        f.brkStack.pop();
        f.setDollarDepth(f.getDollarDepth() - 1);
        return null;
    }

    public Object visitDoWhileStmt(DoWhileStmt ast, Object o) {
        Frame f = (Frame) o;
        String top = f.getNewLabel();
        String bottom = f.getNewLabel();
        f.brkStack.push(bottom);
        f.conStack.push(top);
        emitN("\tbr label %" + top);
        emitN("\n" + top + ":");
        ast.S.visit(this, o);
        if (!ast.S.containsExit) {
            ast.E.visit(this, o);
            int index = ast.E.tempIndex;
            emitN("\tbr i1 %" + index + ", label %" + top + ", label %" + bottom);
        }
        emitN("\n" + bottom+ ":");
        f.brkStack.pop();
        f.conStack.pop();
        return null;
    }

    public Object visitDecimalLiteral(DecimalLiteral ast, Object o) {
        return null;
    }

    public Object visitF32Type(F32Type ast, Object o) {
        emit(LLVM.F32_TYPE);
        return null;
    }

    public Object visitF64Type(F64Type ast, Object o) {
        emit(LLVM.F64_TYPE);
        return null;
    }

    public Object visitF32Expr(F32Expr ast, Object o) {
        Frame f = (Frame) o;
        float fv = Float.parseFloat(ast.DL.spelling);
        String value = String.format("%.21f", fv);
        int num = f.getNewIndex();
        emitN("\t%" + num + " = fadd float 0.0, " + value);
        ast.tempIndex = num;
        return null;
    }

    public Object visitF64Expr(F64Expr ast, Object o) {
        Frame f = (Frame) o;
        float fv = Float.parseFloat(ast.DL.spelling);
        String value = String.format("%.21f", fv);
        int num = f.getNewIndex();
        emitN("\t%" + num + " = fadd double 0.0, " + value);
        ast.tempIndex = num;
        return null;
    }

    public Object visitPointerType(PointerType ast, Object o) {
        ast.t.visit(this, o);
        emit("*");
        return null;
    }

    public Object visitArrayType(ArrayType ast, Object o) {
        emit("[" + ast.length + " x ");
        ast.t.visit(this, o);
        emit("]");
        return null;
    }

    public Object visitArrayInitExpr(ArrayInitExpr ast, Object o) {
        Frame f = (Frame) o;
        int eLength = arrayDetails.length;
        Type innerType = arrayDetails.t;
        int index = 0;

        // handling the empty array case
        if (ast.AL.isEmptyArgList()) {
            while (index < eLength) {
                emitBase(innerType, o);
                int lastIndex = f.localVarIndex - 1;
                int tempIndex = f.getNewIndex();
                emit("\t%" + tempIndex + " = getelementptr inbounds ");
                arrayDetails.visit(this, o);
                emit(", ");
                arrayDetails.visit(this, o);
                emitN("* %" + arrName + ", i32 0, i32 " + index);
                emit("\tstore ");
                innerType.visit(this, o);
                emit(" %" + lastIndex + ", ");
                innerType.visit(this, o);
                emitN("* %" + tempIndex);
                index += 1;
            }
            return null;
        }

        boolean isEmpty = false;
        Args args = (Args) ast.AL;
        Expr finalArg = null;

        currentStructPointerBase.clear();

        while (index < eLength) {

            if (args.E.isStructExpr()) {
                int tempIndex = f.getNewIndex();
                emit("\t%" + tempIndex + " = getelementptr inbounds ");
                arrayDetails.visit(this, o);
                emit(", ");
                arrayDetails.visit(this, o);
                emitN("* %" + arrName + ", i32 0, i32 " + index);
            }
            
            if (isEmpty) {
                finalArg.visit(this, o);
            } else {
                args.E.visit(this, o);
            }

            int lastIndex;
            if (currentStructPointerBase.isEmpty()) {
                lastIndex = f.localVarIndex - 1;
            } else {
                lastIndex = currentStructPointerBase.peek();
            }
            
            if (!args.E.isStructExpr()) {
                int tempIndex = f.getNewIndex();
                emit("\t%" + tempIndex + " = getelementptr inbounds ");
                arrayDetails.visit(this, o);
                emit(", ");
                arrayDetails.visit(this, o);
                emitN("* %" + arrName + ", i32 0, i32 " + index);
                emit("\tstore ");
                innerType.visit(this, o);
                emit(" %" + lastIndex + ", ");
                innerType.visit(this, o);
                emitN("* %" + tempIndex);
            }

            index += 1;

            if (args.EL.isEmptyArgList()) {
                isEmpty = true;
                finalArg = args.E;
            } else {
                args = (Args) args.EL;
            }
        }

        currentStructPointerBase.clear();

        return null;
    }

    public Object visitArrayIndexExpr(ArrayIndexExpr ast, Object o) {

        Type t = ast.type;
        Frame f = (Frame) o;
        ast.index.visit(this, o);
        int index = f.localVarIndex - 1;
        int newIndex = f.getNewIndex();
        emit("\t%" + newIndex + " = getelementptr ");
        if (!ast.I.decl.isParaDecl()) {
            emit("inbounds ");
        }
        Type bT = null;
        if (ast.I.decl.isParaDecl()) {
            bT = ((ArrayType) t).t;
            bT.visit(this, o);
        } else {
            t.visit(this, o);
        }
        emit(", ");
        if (ast.I.decl.isParaDecl()) {
            bT.visit(this, o);
        } else {
            t.visit(this, o);
        }

        if (ast.I.decl instanceof LocalVar L) {
            emit("* %" + ast.I.spelling + L.index);
        } else if (ast.I.decl.isGlobalVar()) {
            emit("* @" + ast.I.spelling);
        } else if (ast.I.decl.isParaDecl()) {
            emit("* %" + ast.I.spelling);
        }

        if (!ast.I.decl.isParaDecl()) {
            emit(", i32 0");
        }
 
        emitN(", i32 %" + index);

        ast.tempIndex = newIndex;
        Type innerT = ((ArrayType) ast.type).t;
        if (!(innerT.isStruct() && ast.inDeclaringLocalVar)) {
            int finalIndex = f.getNewIndex();
            handleLoad(((ArrayType) t).t, newIndex, finalIndex, f);
            ast.tempIndex = finalIndex;
        }
        return null;
    }

    public Object visitI8Type(I8Type ast, Object o) {
        emit(LLVM.I8_TYPE);
        return null;
    }

    public Object visitCharExpr(CharExpr ast, Object o) {
        Frame f = (Frame) o;
        String value = ast.CL.spelling;
        int v = (int) value.charAt(0);
        int num = f.getNewIndex();
        emitN("\t%" + num + " = add i8 0, " + v);
        ast.tempIndex = num;
        return null;
    }

    public Object visitCastExpr(CastExpr ast, Object o) {
        Frame f = (Frame) o;
        ast.E.visit(this, o);
        int numOne = ast.E.tempIndex;
        int temp = f.getNewIndex();
        ast.tempIndex = temp;
        Type from = ast.tFrom, to = ast.tTo;
        emit("\t%" + temp+ " = ");

        if (from.isI64()) {
            if (to.isI32()) {
                emitN("trunc i64 %" + numOne + " to i32");
            } else if (to.isI8()) {
                emitN("trunc i64 %" + numOne + " to i8");
            } else if (to.isF32()) {
                emitN("sitofp i64 %" + numOne + " to float");
            } else if (to.isF64()) {
                emitN("sitofp i64 %" + numOne + " to double");
            }
        }

        if (from.isI32()) {
            if (to.isI64()) {
                emitN("sext i32 %" + numOne + " to i64");
            } else if (to.isI8()) {
                emitN("trunc i32 %" + numOne + " to i8");
            } else if (to.isF32()) {
                emitN("sitofp i32 %" + numOne + " to float");
            } else if (to.isF64()) {
                emitN("sitofp i32 %" + numOne + " to double");
            }
        }

        if (from.isI8()) {
            if (to.isI64()) {
                emitN("sext i8 %" + numOne + " to i64");
            } else if (to.isI32()) {
                emitN("sext i8 %" + numOne + " to i32");
            } else if (to.isF32()) {
                emitN("sitofp i8 %" + numOne + " to float");
            } else if (to.isF64()) {
                emitN("sitofp i8 %" + numOne + " to double");
            }
        }

        if (from.isF32()) {
            if (to.isI64()) {
                emitN("fptosi float %" + numOne + " to i64");
            } else if (to.isI32()) {
                emitN("fptosi float %" + numOne + " to i32");
            } else if (to.isI8()) {
                emitN("fptosi float %" + numOne + " to i8");
            } else if (to.isF64()) {
                emitN("fpext float %" + numOne + " to double");
            }
        }

        return null;
    }

    public Object visitEnumType(EnumType ast, Object o) {
        emit(LLVM.I64_TYPE);
        return null;
    }

    public Object visitEnumExpr(EnumExpr ast, Object o) {
        Frame f = (Frame) o;
        EnumType T = (EnumType) ast.type;
        int value = T.E.getValue(ast.Entry.spelling);
        int num = f.getNewIndex();
        emitN("\t%" + num + " = add i64 0, " + value);
        ast.tempIndex = num;
        return null;
    }

    public Object visitStringExpr(StringExpr ast, Object o) {
        int l = ast.SL.spelling.length() + 1;

        Frame f = (Frame) o;
        int v = f.getNewIndex();
        ast.tempIndex = v;
        emitN("\t%" + v + " = getelementptr [" + l + " x i8], [" + l + " x i8]* @..str" + ast.index + ", i32 0, i32 0");

        if (ast.needToEmit) {
            emitNConst("@..str" + ast.index + " = private constant [" + l + " x i8] c\"" + ast.SL.spelling + "\\00\"");
        }

        return null;
    }

    public Object visitStructElem(StructElem ast, Object o) {
        Type t = ast.T;
        t.visit(this, o);
        return null;
    }

    public Object visitStructList(StructList ast, Object o) {
        ast.S.visit(this, o);
        if (ast.SL.isStructList()) {
            emit(", ");
        }
        ast.SL.visit(this, o);
        return null;
    }

    public Object visitStruct(Struct ast, Object o) {
        if (!ast.isUsed) {
            return null;
        }
        String s;
        s = formattedCurrentPath + "." + ast.I.spelling;
        emit("%struct." + s + " = type { ");
        ast.SL.visit(this, o);
        emitN(" }");
        return null;
    }

    public Object visitStructType(StructType ast, Object o) {
        String name = ast.S.fileName.replace("/", ".");
        emit("%struct." + name + "." + ast.S.I.spelling);
        return null;
    }

    private Stack<Integer> exprDepthValues = new Stack<>();
    private Stack<String> currentStructName = new Stack<>();
    private Stack<Integer> currentStructPointers = new Stack<>();
    private Stack<Integer> currentStructPointerBase = new Stack<>();

    public Object visitStructArgs(StructArgs ast, Object o) {
        Frame f = (Frame) o;

        // Getting the corresponding pointer
        int originalPointer = currentStructPointers.peek();
        int v = f.getNewIndex();
        currentStructPointerBase.push(v);
        emitN("\t%" + v + " = getelementptr %" + currentStructName.peek() + ", %" + currentStructName.peek() +
            "* %" + originalPointer + ", i32 0, i32 " + exprDepthValues.peek());
        int oldExprDepth = exprDepthValues.pop();
        exprDepthValues.push(oldExprDepth + 1);

        if (ast.E.type.isArray()) {
            arrName = String.valueOf(v);
            arrayDetails = (ArrayType) ast.E.type;
        }

        ast.E.visit(this, o);
        if ((!ast.E.isStructExpr() && ast.E.type.isStruct()) || (!ast.E.isArrayInitExpr() && ast.E.type.isArray())) {
            int srcIndex = ast.E.tempIndex;

            int v3 = f.getNewIndex();
            int v4 = f.getNewIndex();

            emit("\t%" + v3 + " = getelementptr ");
            ast.E.type.visit(this, o);
            emit(", ");
            ast.E.type.visit(this, o);
            emitN("* null, i32 1");
            emit("\t%" + v4 + " = ptrtoint ");
            ast.E.type.visit(this, o);
            emitN("* %" + v3 + " to i64");

            emitN("\tcall void @llvm.memcpy.p0.p0.i64(ptr %" + v + ", ptr %" + srcIndex
                + ", i64 %" + v4 + ", i1 false)");
        }
        else if (!(ast.E.isStructExpr() || ast.E.isArrayInitExpr())) {
            int v2 = f.localVarIndex - 1;
            emit("\tstore ");
            ast.E.type.visit(this, o);
            emit(" %" + v2 + ", ");
            ast.E.type.visit(this, o);
            emitN("* %" + v);
        }

        ast.SL.visit(this, o);
        return null;
    }

    public Object visitStructExpr(StructExpr ast, Object o) {
        Frame f = (Frame) o;
        Struct ref = ((StructType) ast.type).S;

        exprDepthValues.push(0);
        String path = ref.fileName.replace("/", ".");
        currentStructName.push("struct." + path + "." + ast.I.spelling);
        currentStructPointers.push(f.localVarIndex - 1);

        ast.SA.visit(this, o);

        currentStructName.pop();
        currentStructPointers.pop();
        exprDepthValues.pop();
        return null;
    }

    public Object visitAssignmentExpr(AssignmentExpr ast, Object o) {
        Frame f = (Frame) o;
        ast.RHS.visit(this, o);
        int rhsIndex = ast.RHS.tempIndex;
        boolean isGlobal = false;
        if (!ast.LHS.isVarExpr()) {
            ast.LHS.parent = ast;
            ast.LHS.visit(this, o);
        }

        int lhsIndex = f.localVarIndex - 1;
        if (ast.RHS.type.isStruct() || (ast.RHS.isArrayIndexExpr() && 
            ((ArrayType) ast.RHS.type).t.isStruct())) {
               
            Type t;
            if (ast.RHS.type.isStruct()) {
                t = ast.RHS.type;
            } else {
                t = ((ArrayType) ast.RHS.type).t;
            }

            int v3 = f.getNewIndex();
            int v4 = f.getNewIndex();

            emit("\t%" + v3 + " = getelementptr ");
            t.visit(this, o);
            emit(", ");
            t.visit(this, o);
            emitN("* null, i32 1");
            emit("\t%" + v4 + " = ptrtoint ");
            t.visit(this, o);
            emitN("* %" + v3 + " to i64");

            String src;
            if (ast.LHS instanceof VarExpr V) {
                SimpleVar VS = (SimpleVar) V.V;
                src = VS.I.spelling + ((Decl) VS.I.decl).index;
            } else {
                if (ast.LHS.isArrayIndexExpr()) {
                    lhsIndex--;
                }
                src = String.valueOf(lhsIndex);
            }
            
            emitN("\tcall void @llvm.memcpy.p0.p0.i64(ptr %" + src + ", ptr %" + rhsIndex 
            + ", i64 %" + v4 + ", i1 false)");
            ast.tempIndex = rhsIndex;
            return null;
        }

        String val = "";
        if (ast.LHS.isArrayIndexExpr()) {
            lhsIndex--;
        } else if (ast.LHS instanceof VarExpr V) {
            SimpleVar VS = (SimpleVar) V.V;
            val = VS.I.spelling + ((Decl) VS.I.decl).index;
            isGlobal = VS.I.decl instanceof GlobalVar;
        }
        emit("\tstore ");
        ast.type.visit(this, o);
        emit(" %" + rhsIndex + ", ");
        ast.type.visit(this, o);
        if (!val.isEmpty()) {
            if (!isGlobal) {
                emit("* %");
            } else {
                emit("* @");
            }
            emitN(val);
        } else {
            emit("* %");
            emitN(String.valueOf(lhsIndex));
        }
        ast.tempIndex = rhsIndex;
        return null;
    }

    public Object visitExprStmt(ExprStmt ast, Object o) {
        ast.E.visit(this, o);
        return null;
    }

    public Object visitEmptyStructAccessList(EmptyStructAccessList ast, Object o) {
        return null;
    }

    public Object visitStructAccessList(StructAccessList ast, Object o) {
        Frame f = (Frame) o;
        int prevIndex = f.localVarIndex - 1;
        if (ast.SAL.isEmptyStructAccessList()) {

            if (ast.arrayIndex.isPresent()) {

                ast.arrayIndex.get().visit(this, o);
                int arrayV = f.localVarIndex - 1;
                int newV = f.getNewIndex();
                emitN("\t%" + newV + " = trunc i64 %" + arrayV + " to i32");

                int v = f.getNewIndex();
                Optional <StructElem> elem = null;
                if (ast.parent.isStructAccess()) {
                    Struct ref = ((StructAccess) ast.parent).ref;
                    elem = ref.getElem(ast.SA.spelling);
                } else if (ast.parent.isStructAccessList()) {
                    Struct ref = ((StructAccessList) ast.parent).ref;
                    elem = ref.getElem(ast.SA.spelling);
                }

                emit("\t%" + v + " = getelementptr inbounds");
                elem.get().T.visit(this, o);
                emit(", ");
                elem.get().T.visit(this, o);
                emitN("* %" + prevIndex + ", i32 0, i32 %" + newV);
            }

            return null;
        }
        Struct ref = ast.ref;
        String file = ast.ref.fileName.replace("/", ".");
        String structName = "struct." + file + "." + ref.I.spelling;
        int newV = f.getNewIndex();
        int index = ast.ref.getNum(((StructAccessList)ast.SAL).SA.spelling);
        emit("\t%" + newV + " = getelementptr %" + structName + ", %" + structName + "* %" + prevIndex);
        emitN(", i32 0, i32 " + index);
        ast.SAL.visit(this, o);
        return null;
    }

    public Object visitStructAccess(StructAccess ast, Object o) {
        Frame f = (Frame) o;

        String file = ast.ref.fileName.replace("/", ".");
        String structName = "struct." + file + "." + ast.ref.I.spelling;

        int arrayIndexNum = -1;
        if (ast.arrayIndex.isPresent()) {
            ast.arrayIndex.get().visit(this, o);
            arrayIndexNum = f.localVarIndex - 1;
          
        }


        int v = f.getNewIndex();
        if (ast.varName.decl instanceof LocalVar L) {

            String localRef = L.I.spelling + L.index;
            if (ast.sourceType.isArray()) {
                emit("\t%" + v + " = getelementptr ");
                ast.sourceType.visit(this, o);
                emit(", ");
                ast.sourceType.visit(this, o);
                emitN("* %" + localRef + ", i32 0, i32 %" + arrayIndexNum);
                localRef = String.valueOf(v);
                v = f.getNewIndex();
            }

            String currentVal = ast.L.SA.spelling;
            int index = ast.ref.getNum(currentVal);
            emit("\t%" + v + " = getelementptr %" + structName + ", %" + structName + "* %" + localRef);
            emitN(", i32 0, i32 " + index);

            ast.L.visit(this, o);
            ast.tempIndex = v;

            if (!(ast.parent.isAssignmentExpr() && ast.isLHSOfAssignment)) {
                int oldV = f.localVarIndex - 1;
                ast.tempIndex = f.getNewIndex();
                handleLoad(ast.type, oldV, ast.tempIndex, f);
            }

        } else if (ast.varName.decl instanceof GlobalVar G) {

        } else if (ast.varName.decl instanceof ParaDecl P) {

            String localRef = P.I.spelling + "0";
            if (ast.sourceType.isArray()) {
                emit("\t%" + v + " = getelementptr ");
                ast.sourceType.visit(this, o);
                emit(", ");
                ast.sourceType.visit(this, o);
                emitN("* %" + localRef + ", i32 0, i32 %" + arrayIndexNum);
                localRef = String.valueOf(v);
                v = f.getNewIndex();
            }

            String currentVal = ast.L.SA.spelling;
            int index = ast.ref.getNum(currentVal);
            emit("\t%" + v + " = getelementptr %" + structName + ", %" + structName + "* %" + localRef);
            emitN(", i32 0, i32 " + index);

            ast.L.visit(this, o);
            ast.tempIndex = v;

            if (!(ast.parent.isAssignmentExpr() && ast.isLHSOfAssignment)) {
                int oldV = f.localVarIndex - 1;
                ast.tempIndex = f.getNewIndex();
                handleLoad(ast.type, oldV, ast.tempIndex, f);
            }

        }

        return null;
    }

    public Object visitDerefExpr(DerefExpr ast, Object o) {
        Frame f = (Frame) o;
        if (ast.E instanceof VarExpr V) {
            SimpleVar VS = (SimpleVar) V.V;
            int v = f.getNewIndex();
            ast.tempIndex = v;
            emit("\t%" + v + " = load ");
            ast.E.type.visit(this, o);
            emit(", ");
            ast.E.type.visit(this, o);
            if (VS.I.decl instanceof LocalVar L) {
                emitN("* %" + VS.I.spelling + L.index);
            } else if (VS.I.decl.isGlobalVar()) {
                emitN("* @" + VS.I.spelling);
            } else if (VS.I.decl.isParaDecl()) {
                emitN("* %" + VS.I.spelling + "0");
            }
            // Need to load the actual value;
            if (!ast.isLHSOfAssignment) {
                Type innerT = ((PointerType) ast.E.type).t;
                int v2 = f.getNewIndex();
                ast.tempIndex = v2;
                emit("\t%" + v2 + " = load ");
                innerT.visit(this, o);
                emit(", ");
                ast.E.type.visit(this, o);
                emitN(" %"  + v);
            }
        } else {
            System.out.println("handle more advanced derefs...");
        }
        return null;
    }

    public void emitBase(Type t, Object o) {
        // TODO: handle other cases
        Expr I;
        if (t.isI64()) {
            I = new I64Expr(new IntLiteral("0", dummyPos), dummyPos);
        } else if (t.isBoolean()) {
            I = new BooleanExpr(new BooleanLiteral("false", dummyPos), dummyPos);
        } else if (t.isF32()) {
            I = new F32Expr(new DecimalLiteral("1.0", dummyPos), dummyPos);
        } else {
            return;
        }
        I.visit(this, o);
    }

    public int handleBitCast(Type t, String name, Frame f) {
        int v = f.getNewIndex();
        emit("\t%" + v + " = bitcast ");
        t.visit(this, f);
        emit("* %" + name + " to ");
        t.visit(this, f);
        emitN("*");
        return v;
    }

    public void handleLoad(Type t, int v1, int v2, Frame o) {
        emit("\t%" + v2 + " = load ");
        t.visit(this, o);
        emit(", ");
        t.visit(this, o);
        emitN("* %" + v1);
    }

    public void handleLoad(Type t, int v1, String v2, Frame o) {
        emit("\t%" + v2 + " = load ");
        t.visit(this, o);
        emit(", ");
        t.visit(this, o);
        emitN("* %" + v1);
    }

    public void handleLoad(Type t, String v1, int v2, Frame o) {
        emit("\t%" + v2 + " = load ");
        t.visit(this, o);
        emit(", ");
        t.visit(this, o);
        emitN("* %" + v1);
    }

    public void emit(String s) {
        LLVM.append(new Instruction(s));
    }

    public void emitConst(String s) {
        LLVM.appendConstant(new Instruction(s));
    }

    public void emitN(String s) {
        LLVM.append(new Instruction(s + "\n"));
    }
    public void emitNConst(String s) {
        LLVM.appendConstant(new Instruction(s + "\n"));
    }

    public Object visitSizeOfExpr(SizeOfExpr ast, Object o) {
        
        Type t;
        if (ast.typeV.isPresent()) {
            t = ast.typeV.get();
        } else {
            t = ast.varType;
        }

        int v =handleSizeOfExpr(t, o);
        ast.tempIndex = v;

        return null;
    }

    private int handleSizeOfExpr(Type t, Object o) {
        Frame f = (Frame) o;
        int size = -1;

        if (t.isI64() || t.isEnum() || t.isF64()) {
            size = 8;
        } else if (t.isF32() || t.isI32()) {
            size = 4;
        }else if (t.isI8() || t.isBoolean()) {
            size = 1;
        } else if (t.isPointer()) {
            size = 8;
        } else if (t.isStruct()) {
            StructType T = (StructType) t;

            int v = f.getNewIndex();
            int v4 = f.getNewIndex();

            emit("\t%" + v + " = getelementptr ");
            T.visit(this, o);
            emit(", ");
            T.visit(this, o);
            emitN("* null, i32 1");
            emit("\t%" + v4 + " = ptrtoint ");
            T.visit(this, o);
            emitN("* %" + v + " to i64");

            return v4;

        } else if (t.isArray()) {
            Type innerT = ((ArrayType) t).t;
            int v2 = handleSizeOfExpr(innerT, o);
            int v = f.getNewIndex();
            emitN("\t%" + v + " = mul i64 %" + v2 + ", " + ((ArrayType) t).length);
            return v;
        }

        int v = f.getNewIndex();
        emit("\t%" + v + " = ");
        emitN("add i64 0, " + size);
        return v; 
    }

    public Object visitModule(Module ast, Object o) {
        return null;
    }

    public Object visitProgram(Program ast, Object o) {
        return null;
    }

    public Object visitIdent(Ident ast, Object o) {
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

    public Object visitEmptyStmtList(EmptyStmtList ast, Object o) {
        return null;
    }

    public Object visitBooleanLiteral(BooleanLiteral ast, Object o) {
        return null;
    }

    public Object visitErrorType(ErrorType ast, Object o) {
        return null;
    }

    public Object visitIntLiteral(IntLiteral ast, Object o) {
        return null;
    }

    public Object visitArgList(Args ast, Object o) {
        return null;
    }

    public Object visitEmptyCompStmt(EmptyCompStmt ast, Object o) {
        return null;
    }

    public Object visitEmptyDeclList(EmptyDeclList ast, Object o) {
        return null;
    }

    public Object visitCharLiteral(CharLiteral ast, Object o) {
        return null;
    }

    public Object visitStringLiteral(StringLiteral ast, Object o) {
        return null;
    }

    public Object visitEnum(Enum ast, Object o) {
        return null;
    }

    public Object visitMurkyType(MurkyType ast, Object o) {
        return null;
    }

    public Object visitDotExpr(DotExpr ast, Object o) {
        return null;
    }

    public Object visitUnknownType(UnknownType ast, Object o) {
        return null;
    }

    public Object visitEmptyStructList(EmptyStructList ast, Object o) {
        return null;
    }

    public Object visitEmptyStructArgs(EmptyStructArgs ast, Object o) {
        return null;
    }

    public Object visitImportStmt(ImportStmt ast, Object o) {
        return null;
    }

    public Object visitAnyType(AnyType ast, Object o) {
        return null;
    }

    public Object visitIntExpr(IntExpr ast, Object o) {
        return null;
    }   

    public Object visitDecimalExpr(DecimalExpr ast, Object o) {
        return null;
    }
}
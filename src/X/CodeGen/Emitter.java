package X.CodeGen;

import X.Evaluator.Evaluator;
import X.Lexer.Position;
import X.Nodes.*;
import X.Nodes.Enum;

public class Emitter implements Visitor {

    private final String outputName;
    private ArrayType arrayDetails;
    private String arrName = "";
    private final Position dummyPos = new Position();
    private boolean seenStructs = false;
    private String currentStructName = "";
    private String currentStructType = "";


    public Emitter(String outputName) {
        this.outputName = outputName;
    }

    public final void gen(AST ast) {

        // Probably inefficient, think of a way to hoist structs to top of AST
        // TODO: will work for now though
        DeclList L = (DeclList) ((Program) ast).PL;
        while (true) {
            if (L.D instanceof Struct S) {
                S.visit(this, null);
            }
            if (L.DL instanceof EmptyDeclList) {
                break;
            }
            L = (DeclList) L.DL;
        }
        seenStructs = true;

        emitN("declare i32 @printf(i8*, ...)");
        emitN("declare i32 @scanf(i8*, ...)");
        emitN("@.Istr = constant [4 x i8] c\"%d\\0A\\00\"");
        emitN("@.Cstr = constant [4 x i8] c\"%c\\0A\\00\"");
        emitN("@.IFstr = constant [6 x i8] c\"%.2f\\0A\\00\"");
        emitN("@.IIstr = private unnamed_addr constant [3 x i8] c\"%d\\00\"");
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
        if (!ast.isUsed && !ast.I.spelling.equals("main")) {
            return null;
        }
        Frame f = new Frame(ast.I.spelling.equals("main"));
        emit("define ");
        ast.T.visit(this, f);
        emit(" @" + ast.I.spelling);
        if (!ast.I.spelling.equals("main")) {
            emit("." + ast.TypeDef);
        }
        ast.PL.visit(this, f);
        emitN(" {");

        // Bind mutable variables to a local variable
        List PL = ast.PL;
        while (!(PL instanceof EmptyParaList)) {
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
           emitN("\tret i32 0");
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

        if (ast.T.isArray()) {
            arrayDetails = (ArrayType) ast.T;
            arrName = ast.I.spelling + depth;
            Expr E = ast.E;
            if (E instanceof EmptyExpr) {
                E = new ArrayInitExpr(new EmptyArgList(dummyPos), dummyPos);
            }
            E.visit(this, o);
            return null;
        } else if (ast.T.isStruct()) {
            currentStructName = ast.I.spelling + depth;
            currentStructType = "struct." + ((StructType) ast.T).S.I.spelling;
            ast.E.visit(this, o);
            return null;
        }

        if (ast.E instanceof EmptyExpr) {
            // TODO: assign default values
            return null;
        }

        // Declare a 'char *'
        if (ast.T.isPointer() && ((PointerType) ast.T).t.isChar() && ast.E instanceof StringExpr) {
            ast.E.visit(this, o);
            int l = ((StringExpr) ast.E).SL.spelling.length() + 1;
            int n = ((StringExpr) ast.E).index;
            emitN("\tstore i8 * getelementptr inbounds ([" + l + " x i8], [" + l + " x i8]* @..str" + n +
                    " , i64 0, i64 0), i8** %" + ast.I.spelling + depth);
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

        ast.S2.visit(this, o);
        emitN("\n" + elseC + ":");
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
        if (!(ast.E2 instanceof EmptyExpr)) {
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
            case "c-", "i-", "f-" -> {
                if (ast.parent instanceof BinaryExpr parent) {
                    int numOne = parent.E1.tempIndex;
                    int numTwo = parent.E2.tempIndex;
                    int newNum = f.getNewIndex();
                    parent.tempIndex = newNum;
                    if (ast.spelling.equals("c-")) {
                        emitN("\t%" + newNum + " = sub i8 %" + numOne + ", %" + numTwo);
                    } else {
                        emitN("\t%" + newNum + " = sub i32 %" + numOne + ", %" + numTwo);
                    }
                } else if (ast.parent instanceof UnaryExpr parent) {
                    int numOne = parent.E.tempIndex;
                    int newNum = f.getNewIndex();
                    parent.tempIndex = newNum;
                    if (ast.spelling.equals("c-")) {
                        emitN("\t%" + newNum + " = sub i8 0, %" + numOne);
                    } else {
                        emitN("\t%" + newNum + " = sub i32 0, %" + numOne);
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
            case "i+", "f+", "c+", "i*", "f*", "c*", "i%", "ii%", "c%", "i/", "f/", "c/", "i==", "f==", "b==", "c==", "i!=", "f!=", "c!=", "b!=",
                "i<", "f<", "c<", "i<=", "f<=", "c<=", "i>", "f>", "c>", "i>=", "f>=", "c>=" -> {
                if (ast.parent instanceof BinaryExpr parent) {
                    int numOne = parent.E1.tempIndex;
                    int numTwo = parent.E2.tempIndex;
                    int newNum = f.getNewIndex();
                    parent.tempIndex = newNum;
                    emitN("\t%" + newNum + " = " +  opToCommand(ast.spelling) +
                        " %" + numOne + ", %" + numTwo);
                }
            }
            default -> {}
        }
        return null;
    }

    public String opToCommand(String input) {
        return switch (input)  {
            case "i+" ->  "add i32";
            case "c+" ->  "add i8";
            case "f+" ->  "fadd float";
            case "i*" -> "mul i32";
            case "c*" -> "mul i8";
            case "f*" -> "fmul float";
            case "i%", "ii%" -> "srem i32";
            case "c%" -> "srem i8";
            case "i/" -> "sdiv i32";
            case "c/" -> "sdiv i8";
            case "f/" -> "fdiv float";
            case "i==" -> "icmp eq i32";
            case "c==" -> "icmp eq i8";
            case "f==" -> "fcmp eq float";
            case "b==" -> "icmp eq i1";
            case "i!=" -> "icmp ne i32";
            case "c!=" -> "icmp ne i8";
            case "f!=" -> "fcmp ne float";
            case "b!=" -> "icmp ne i1";
            case "i<=" -> "icmp sle i32";
            case "c<=" -> "icmp sle i8";
            case "f<=" -> "fcmp sle float";
            case "i<" -> "icmp slt i32";
            case "c<" -> "icmp slt i8";
            case "f<" -> "fcmp slt float";
            case "i>" -> "icmp sgt i32";
            case "c>" -> "icmp sgt i8";
            case "f>" -> "fcmp sgt float";
            case "i>=" -> "icmp sge i32";
            case "c>=" -> "icmp sge i8";
            case "f>=" -> "fcmp sge float";
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
            // Don't entirely get this, just looked at LLVM output for C++
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
            // Don't entirely get this, just looked at LLVM output for C++
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
            int index = f.getNewIndex();
            ast.tempIndex = index;
            emit("\t%" + index + " = bitcast ");
            d.T.visit(this, o);
            emit("* %" + SV.I.spelling + d.index + " to ");
            d.T.visit(this, o);
            emitN("*");
            return null;
        }
        ast.E.visit(this, o);
        ast.O.visit(this, o);
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
        List SL = ast;
        while (true) {
            ((StmtList) SL).S.visit(this, o);
            if (((StmtList) SL).SL instanceof EmptyStmtList) {
                break;
            } else if (((StmtList) SL).S instanceof ReturnStmt) {
                break;
            } else {
                SL = ((StmtList) SL).SL;
            }
        }
        return null;
    }

    public Object visitEmptyStmtList(EmptyStmtList ast, Object o) {
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
        Frame f = (Frame) o;
        String value = ast.IL.spelling;
        int num = f.getNewIndex();
        emitN("\t%" + num + " = add i32 0, " + value);
        ast.tempIndex = num;
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

    public Object visitEmptyCompStmt(EmptyCompStmt ast, Object o) {
        return null;
    }

    public Object visitEmptyDeclList(EmptyDeclList ast, Object o) {
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
            emitN("\t%" + newIndex + " = load i32, i32* %" + "$".repeat(f.getDollarDepth()));
            return null;
        }

        AST d = ast.I.decl;
        if (d instanceof LocalVar) {
            int newIndex = f.getNewIndex();
            if (((LocalVar) d).T.isArray()) {
                ArrayType t = (ArrayType) ((LocalVar) d).T;
                emit("\t%" + newIndex + " = getelementptr inbounds ");
                int length = t.length;
                ((LocalVar) d).T.visit(this, length);
                emit(", ");
                ((LocalVar) d).T.visit(this, length);
                emitN("* %" + ast.I.spelling + ((LocalVar) ast.I.decl).index + ", i32 0, i32 0");
                return null;
            }
            emit("\t%" + newIndex + " = load ");
            ((LocalVar) d).T.visit(this, o);
            emit(", ");
            ((LocalVar) d).T.visit(this, o);
            emitN("* %" + ast.I.spelling + ((LocalVar) ast.I.decl).index);
        } else if (d instanceof ParaDecl) {
            if (((ParaDecl) d).isMut) {
                int newIndex = f.getNewIndex();
                emit("\t%" + newIndex + " = load ");
                ((ParaDecl) d).T.visit(this, o);
                emit(", ");
                ((ParaDecl) d).T.visit(this, o);
                emitN("* %" + ast.I.spelling + ((ParaDecl) ast.I.decl).index);
                return null;
            }

            int newIndex = f.getNewIndex();
            emit("\t%" + newIndex + " = ");
            Type T = ((ParaDecl) d).T;
            if (T.isInt() || T.isBoolean() || T.isChar() || T.isEnum()) {
                emit("add ");
                T.visit(this, o);
                emit(" 0, ");
            }
            emitN(" %" + ast.I.spelling + "0");
        } else if (d instanceof GlobalVar) {
            int newIndex = f.getNewIndex();
            emit("\t%" + newIndex + " = load ");
            ((GlobalVar) d).T.visit(this, o);
            emit(", ");
            ((GlobalVar) d).T.visit(this, o);
            emitN("* @" + ast.I.spelling);
        }
        return null;
   }

    public void handleOutInt(CallExpr ast, Object o) {
        Frame f = (Frame) o;
        ((Args) ast.AL).E.visit(this, o);
        int index = ((Args) ast.AL).E.tempIndex;
        int indexStr = f.getNewIndex();
        int newIndex = f.getNewIndex();
        emitN("\t%" + indexStr + " = getelementptr [4 x i8], [4 x i8]* @.Istr, i32 0, i32 0");
        emitN("\t%" + newIndex + " = call i32 (i8*, ...) @printf(i8* %" + indexStr + ", i32 %" + index + ")");
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

    public void handleOutFloat(CallExpr ast, Object o) {
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

    public Object visitCallExpr(CallExpr ast, Object o) {

        if (ast.I.spelling.equals("outInt")) {
            handleOutInt(ast, o);
            return null;
        }

        if (ast.I.spelling.equals("outChar")) {
            handleOutChar(ast, o);
            return null;
        }

        if (ast.I.spelling.equals("outFloat")) {
            handleOutFloat(ast, o);
            return null;
        }

        Frame f = (Frame) o;

        // Evaluate all the expressions
        if (!(ast.AL instanceof EmptyArgList)) {
            Args AL = (Args) ast.AL;
            while (true) {
                AL.E.visit(this, o);
                if (AL.EL instanceof EmptyArgList) {
                    break;
                }
                AL = (Args) AL.EL;
            }
        }

        Function functionRef = (Function) ast.I.decl;
        if (functionRef.T.isVoid()) {
            emit("\tcall ");
        } else {
            int num = f.getNewIndex();
            ast.tempIndex = num;
            emit("\t%" + num + " = call ");
        }
        functionRef.T.visit(this, o);
        emit(" @" + ast.I.spelling + "." + ast.TypeDef);

        emit("(");
        if (!(ast.AL instanceof EmptyArgList)) {
            Args AL = (Args) ast.AL;
            while (true) {
                Expr E = AL.E;
                int index = E.tempIndex;
                // pass arrays by ref, not val
                if (E.type.isArray()) {
                    if (E instanceof ArrayIndexExpr) {
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
                if (AL.EL instanceof EmptyArgList) {
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
            emitN("\t%" + dollars + " = alloca i32");
            if (ast.I2.isPresent()) {
                ast.I1.get().visit(this, o);
                int lowerIndex = ast.I1.get().tempIndex;
                newIndex = f.getNewIndex();
                emitN("\t%" + newIndex + " = add i32 0, %" + lowerIndex);
            } else {
                newIndex = f.getNewIndex();
                emitN("\t%" + newIndex + " = add i32 0, 0");
            }
            emitN("\tstore i32 %" + newIndex + ", i32* %" + dollars);
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
            emitN("\t%" + dolIndex + " = load i32, i32* %" + dollars);
            int boolIndex = f.getNewIndex();
            emitN("\t%" + boolIndex + " = icmp sge i32 %" + dolIndex + ", %" + tempIndex);
            emitN("\tbr i1 %" + boolIndex + ", label %" + bottomLabel + ", label %" + midLabel);
            emitN(midLabel + ":");

            ast.S.visit(this, o);
            int dol2Index = f.getNewIndex();
            int newIndexTwo = f.getNewIndex();

            emitN("\tbr label %" + iterateLabel);
            emitN(iterateLabel + ":");
            emitN("\t%" + dol2Index + " = load i32, i32* %" + dollars);
            emitN("\t%" + newIndexTwo + " = add i32 1, %" + dol2Index);
            emitN("\tstore i32 %" + newIndexTwo +", i32* %" + dollars);
            emitN("\tbr label %" + topLabel);
            emitN(bottomLabel + ":");
        } else {
            // no bounds
            newIndex = f.getNewIndex();
            emitN("\t%" + dollars + " = alloca i32");
            emitN("\t%" + newIndex + " = add i32 0, 0");
            emitN("\tstore i32 %" + newIndex + ", i32* %" + dollars);
            emitN("\tbr label %" + topLabel);
            emitN(topLabel + ":");
            ast.S.visit(this, o);
            int dol2Index = f.getNewIndex();

            emitN("\tbr label %" + iterateLabel);
            emitN(iterateLabel + ":");
            emitN("\t%" + dol2Index + " = load i32, i32* %" + dollars);
            int newIndexTwo = f.getNewIndex();
            emitN("\t%" + newIndexTwo + " = add i32 1, %" + dol2Index);
            emitN("\tstore i32 %" + newIndexTwo +", i32* %" + dollars);
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

    public Object visitFloatLiteral(FloatLiteral ast, Object o) {
        return null;
    }

    public Object visitFloatType(FloatType ast, Object o) {
        emit(LLVM.FLOAT_TYPE);
        return null;
    }

    public Object visitFloatExpr(FloatExpr ast, Object o) {
        Frame f = (Frame) o;
        String value = ast.FL.spelling;
        int num = f.getNewIndex();
        emitN("\t%" + num + " = fadd float 0.0, " + value);
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

        if (ast.AL instanceof EmptyArgList) {
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
        while (index < eLength) {
            if (isEmpty) {
                finalArg.visit(this, o);
            } else {
                args.E.visit(this, o);
            }
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

            if (args.EL instanceof EmptyArgList) {
                isEmpty = true;
                finalArg = args.E;
            } else {
                args = (Args) args.EL;
            }
        }
        return null;
    }

    public Object visitArrayIndexExpr(ArrayIndexExpr ast, Object o) {

        Type t = ast.type;
        Frame f = (Frame) o;
        ast.index.visit(this, o);
        int index = f.localVarIndex - 1;
        int newIndex = f.getNewIndex();
        emit("\t%" + newIndex + " = getelementptr ");
        if (!(ast.I.decl instanceof ParaDecl)) {
            emit("inbounds ");
        }
        Type bT = null;
        if (ast.I.decl instanceof ParaDecl) {
            bT = ((ArrayType) t).t;
            bT.visit(this, o);
        } else {
            t.visit(this, o);
        }
        emit(", ");
        if (ast.I.decl instanceof ParaDecl) {
            bT.visit(this, o);
        } else {
            t.visit(this, o);
        }
        if (ast.I.decl instanceof LocalVar) {
            emit("* %" + ast.I.spelling + ((LocalVar) ast.I.decl).index);
        } else if (ast.I.decl instanceof GlobalVar) {
            emit("* @" + ast.I.spelling);
        } else if (ast.I.decl instanceof ParaDecl) {
            emit("* %" + ast.I.spelling);
        }
        if (!(ast.I.decl instanceof ParaDecl)) {
            emit(", i32 0");
        }
        emitN(", i32 %" + index);
        int finalIndex = f.getNewIndex();
        ast.tempIndex = finalIndex;
        emit("\t%" + finalIndex + " = load ");
        ((ArrayType) t).t.visit(this, o);
        emit(", ");
        ((ArrayType) t).t.visit(this, o);
        emitN("* %" + newIndex);
        return null;
    }

    public Object visitCharType(CharType ast, Object o) {
        emit(LLVM.CHAR_TYPE);
        return null;
    }

    public Object visitCharLiteral(CharLiteral ast, Object o) {
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
        if (from.isInt() && to.isFloat()) {
            emitN("sitofp i32 %" + numOne + " to float");
        }
        if (from.isChar() && to.isInt()) {
            emitN("sext i8 %" + numOne + " to i32");
        }
        if (from.isInt() && to.isChar()) {
            emitN("trunc i32 %" + numOne + " to i8");
        }
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

    public Object visitEnumType(EnumType ast, Object o) {
        emit(LLVM.INT_TYPE);
        return null;
    }

    public Object visitEnumExpr(EnumExpr ast, Object o) {
        Frame f = (Frame) o;
        EnumType T = (EnumType) ast.type;
        int value = T.E.getValue(ast.Entry.spelling);
        int num = f.getNewIndex();
        emitN("\t%" + num + " = add i32 0, " + value);
        ast.tempIndex = num;
        return null;
    }

    public Object visitUnknownType(UnknownType ast, Object o) {
        return null;
    }

    public Object visitStringExpr(StringExpr ast, Object o) {
        if (!ast.needToEmit) {
            return  null;
        }

        int l = ast.SL.spelling.length() + 1;
        emitNConst("@..str" + ast.index + " = private constant [" + l + " x i8] c\"" + ast.SL.spelling + "\\00\"");
        return null;
    }

    public Object visitStructElem(StructElem ast, Object o) {
        Type t = ast.T;
        t.visit(this, o);
        return null;
    }

    public Object visitStructList(StructList ast, Object o) {
        ast.S.visit(this, o);
        if (ast.SL instanceof StructList) {
            emit(", ");
        }
        ast.SL.visit(this, o);
        return null;
    }

    public Object visitStruct(Struct ast, Object o) {
        if (!ast.isUsed || seenStructs) {
            return null;
        }
        String s = ast.I.spelling;
        emit("%struct." + s + " = type { ");
        ast.SL.visit(this, o);
        emitN(" }");
        return null;
    }

    public Object visitEmptyStructList(EmptyStructList ast, Object o) {
        return null;
    }

    public Object visitStructType(StructType ast, Object o) {
        emit("%struct." + ast.S.I.spelling);
        return null;
    }

    public Object visitEmptyStructArgs(EmptyStructArgs ast, Object o) {
        return null;
    }

    public Object visitStructArgs(StructArgs ast, Object o) {

        // Assuming that the struct expr is only mapped to a decl currently
        String parentName = currentStructName;
        Frame f = (Frame) o;

        ast.E.visit(this, o);
        int lastIndex = f.localVarIndex - 1;
        int ptrIndex = f.getNewIndex();
        emit("\t%" + ptrIndex + " = getelementptr %");
        emit(currentStructType);
        emit(", %");
        emit(currentStructType);
        emitN("* %" + parentName + ", i32 0, i32 " + ast.structIndex);
        emit("\tstore ");
        ast.E.type.visit(this, o);
        emit(" %" + lastIndex + ", ");
        ast.E.type.visit(this, o);
        emitN("* %" + ptrIndex);
        ast.SL.visit(this, o);
        return null;
    }

    public Object visitStructExpr(StructExpr ast, Object o) {
        ast.SA.visit(this, o);
        return null;
    }

    private void handleStandardDeclaration(AssignmentExpr ast, Object o) {
    }

    private void handleArrayDeclaration(AssignmentExpr ast, Object o) {
    }

    public Object visitAssignmentExpr(AssignmentExpr ast, Object o) {
        Frame f = (Frame) o;
        ast.RHS.visit(this, o);
        int rhsIndex = ast.RHS.tempIndex;
        boolean isGlobal = false;
        if (!(ast.LHS instanceof VarExpr)) {
            ast.LHS.visit(this, o);
        }
        int lhsIndex = f.localVarIndex - 1;
        String val = "";
        if (ast.LHS instanceof ArrayIndexExpr) {
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

    public Object visitDerefExpr(DerefExpr ast, Object o) {
        Frame f = (Frame) o;
        emitN("; A");
        if (ast.E instanceof VarExpr V) {
            SimpleVar VS = (SimpleVar) V.V;
            int v = f.getNewIndex();
            ast.tempIndex = v;
            emit("\t%" + v + " = load ");
            ast.E.type.visit(this, o);
            emit(", ");
            ast.E.type.visit(this, o);
            if (VS.I.decl instanceof LocalVar) {
                emitN("* %" + VS.I.spelling + ((LocalVar) VS.I.decl).index);
            } else if (VS.I.decl instanceof GlobalVar) {
                emitN("* @" + VS.I.spelling);
            } else if (VS.I.decl instanceof ParaDecl) {
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
        emitN("; A");
        return null;
    }

    public void emitBase(Type t, Object o) {
        // TODO: handle other cases
        Expr I;
        if (t.isInt()) {
            I = new IntExpr(new IntLiteral("0", dummyPos), dummyPos);
        } else if (t.isBoolean()) {
            I = new BooleanExpr(new BooleanLiteral("false", dummyPos), dummyPos);
        } else if (t.isFloat()) {
            I = new FloatExpr(new FloatLiteral("1.0", dummyPos), dummyPos);
        } else {
            return;
        }
        I.visit(this, o);
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
}
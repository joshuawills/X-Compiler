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

    public boolean inCallOrMethodAccessExpr = false;

    boolean inMainFunction = false;

    public Emitter(String outputName) {
        this.outputName = outputName;
    }

    public AllModules modules;
    public Module currentModule;

    private boolean inLibCDeclarations = false;
    private boolean inTupleDeclarations = false;
    private boolean inSubTuple = false;
    private boolean declaringTopTuple = false;

    public final void gen() {

        modules = AllModules.getInstance();
        ArrayList<Module> allModules = modules.getModules();

        emitN("declare i32 @scanf(i8*, ...)");

        for (String s: modules.stringConstantsMapping.keySet()) {
            int l = s.length() + 1;
            emitN("@..str" + modules.stringConstantsMapping.get(s) + " = private constant [" + l + " x i8] c\"" + s + "\\00\"");
        }

        // Set up all the libc functions
        inLibCDeclarations = true;
        for (Function f: modules.getLibCFunctions()) {
            f.visit(this, null);
        }
        for (GlobalVar v: modules.getLibCVariables()) {
            v.visit(this, null);
        }
        inLibCDeclarations = false;

        // Instantiate all the tuple types
        inTupleDeclarations = true;
        inSubTuple = true;
        for (TupleType t: modules.getTupleTypes()) {
            if (t.inAnotherTupleType) {
                t.visit(this, null);
            }
        }
        inSubTuple = false;
        for (TupleType t: modules.getTupleTypes()) {
            declaringTopTuple = false;
            t.visit(this, null);
            declaringTopTuple = false;
        }
        inTupleDeclarations = false;


        // Visiting all the structs
        for (Module m: allModules) {
            for (Struct s: m.getStructs().values()) {
                s.visit(this, null);
            }
        }

        // Visiting all the global vars
        for (Module m: allModules) {
            for (GlobalVar v: m.getVars().values()) {
                v.visit(this, null);
            }
        }
        
        // Visiting all the functions and methods
        for (Module m: allModules) {
            currentModule = m;

            for (Function f: m.getFunctions()) {
                f.visit(this, null);
            }

            for (Method f: m.getMethods()) {
                currentModule = m;
                f.visit(this, null);
            }
        }

        for (Impl I: modules.getImpls()) {
            currentModule = modules.getModule(I.filename);
            I.visit(this, null);
        }

        LLVM.dump(outputName);
    }

    public void handleLibC(Function ast, Frame f) {
        emit("declare ");
        ast.T.visit(this, f);
        emit(" @");
        emit(ast.I.spelling);
        emit("(");
        ast.PL.visit(this, f);
        emitN(")");
    }

    public Object visitMethod(Method ast, Object o) {
        if (!ast.isUsed) {
            return null;
        }

        Frame f = new Frame(false);
        inMainFunction = false;

        emit("define ");
        if (ast.T.isStruct() || ast.T.isTuple()) {
            emit("void");
        } else {
            ast.T.visit(this, f);
        }

        emit(" @");
        String path = ast.filename.replace("/", ".");
        emit(path);
        emit(ast.I.spelling + "." + ast.attachedStruct.T.getMini() + "." + ast.TypeDef);
        
        emit("(");
        if (ast.T.isStruct() || ast.T.isTuple()) {
            emit("ptr %.ret, "); 
        }
        ast.attachedStruct.visit(this, o);
        if (!ast.PL.isEmptyParaList()) {
            emit(", ");
        }
        ast.PL.visit(this, f);
        emitN(") {");

        Type t1 = ast.attachedStruct.T;
        String n = ast.attachedStruct.I.spelling;
        if ((ast.attachedStruct.isMut && !t1.isArray() && !t1.isStruct() && !t1.isTuple()) || t1.isPointer()) {
            emit("\t%" + n + "0 = alloca ");
            t1.visit(this, o);

            emit("\n\tstore ");
            t1.visit(this, o);
            emit(" %" + n  + ", ");
            t1.visit(this, o);
            if (t1.isPointer()) {
                emitN(" %" + n + "0");
            } else {
                emitN("* %" + n + "0");
            }
        }

        if (t1.isStruct() || t1.isTuple()) {
            emit("\t%" + n + "0 = alloca ");
            t1.visit(this, o);
            emitN("");
            
            int v = handleSizeOfExpr(t1, f);
            emitN("\tcall void @llvm.memcpy.p0.p0.i64(ptr %" + n + "0, ptr %" + n + ", i64 %" + v + ", i1 false)");
        }

        // Bind mutable variables to a local variable
        List PL = ast.PL;
        while (!PL.isEmptyParaList()) {
            ParaDecl P = ((ParaList) PL).P;
            Type t = P.T;
            if ((P.isMut && !t.isArray() && !t.isStruct() && !t.isTuple()) || t.isPointer()) {
                emit("\t%" + P.I.spelling + "0 = alloca ");
                t.visit(this, o);

                emit("\n\tstore ");
                t.visit(this, o);
                emit(" %" + P.I.spelling + ", ");
                t.visit(this, o);
                if (t.isPointer()) {
                    emitN(" %" + P.I.spelling + "0");
                } else {
                    emitN("* %" + P.I.spelling + "0");
                }
            }

            if (t.isStruct() || t.isTuple()) {
                emit("\t%" + P.I.spelling + "0 = alloca ");
                t.visit(this, o);
                emitN("");
               
                int v = handleSizeOfExpr(t, f);
                emitN("\tcall void @llvm.memcpy.p0.p0.i64(ptr %" + P.I.spelling + "0, ptr %" + P.I.spelling + ", i64 %" + v + ", i1 false)");
            }

            PL = ((ParaList) PL).PL;
        }

        ast.S.visit(this, f);
        if (ast.I.spelling.equals("main")) {
           emitN("\tret i64 0");
        }

        if (!ast.S.containsExit && ast.T.isVoid() && !inMainFunction) {
            emitN("\t ret void");
        }

        emitN("}");
        inMainFunction = false;
        return null;

    }

    public Object visitFunction(Function ast, Object o) {

        Frame f = new Frame(ast.I.spelling.equals("main"));
        inMainFunction = ast.I.spelling.equals("main");
        if (inLibCDeclarations) {
            handleLibC(ast, f);
            return null;
        }

        if (!ast.isUsed && !ast.I.spelling.equals("main")) {
            return null;
        }
        emit("define ");
        if (ast.T.isStruct() || ast.T.isTuple()) {
            emit("void");
        } else if (ast.I.spelling.equals("main")) {
            emit("i64");
        } else {
            ast.T.visit(this, f);
        }

        String path = ast.filename.replace("/", ".");
        if (ast.I.spelling.equals("main")) {
            emit(" @main");
        } else {
            emit(" @" + path + ast.I.spelling);
        }

        if (!ast.I.spelling.equals("main")) {
            emit("." + ast.TypeDef);
        }

        emit("(");
        if (ast.T.isStruct() || ast.T.isTuple()) {
            emit("ptr %.ret"); 
            if (!ast.PL.isEmptyParaList()) {
                emit(", ");
            }
        }
        ast.PL.visit(this, f);
        emit(")");

        emitN(" {");

        // Bind mutable variables to a local variable
        List PL = ast.PL;
        while (!PL.isEmptyParaList()) {
            ParaDecl P = ((ParaList) PL).P;
            Type t = P.T;
            if ((P.isMut && !t.isArray() && !t.isStruct() && !t.isTuple()) || t.isPointer()) {
                emit("\t%" + P.I.spelling + "0 = alloca ");
                t.visit(this, o);

                emit("\n\tstore ");
                t.visit(this, o);
                emit(" %" + P.I.spelling + ", ");
                t.visit(this, o);
                if (t.isPointer()) {
                    emitN(" %" + P.I.spelling + "0");
                } else {
                    emitN("* %" + P.I.spelling + "0");
                }
            }

            if (t.isStruct() || t.isTuple()) {
                emit("\t%" + P.I.spelling + "0 = alloca ");
                t.visit(this, o);
                emitN("");
               
                int v = handleSizeOfExpr(t, f);
                emitN("\tcall void @llvm.memcpy.p0.p0.i64(ptr %" + P.I.spelling + "0, ptr %" + P.I.spelling + ", i64 %" + v + ", i1 false)");
            }

            PL = ((ParaList) PL).PL;
        }

        ast.S.visit(this, f);
        if (ast.I.spelling.equals("main")) {
           emitN("\tret i64 0");
        }

        if (!ast.S.containsExit && ast.T.isVoid() && !inMainFunction) {
            emitN("\t ret void");
        }

        emitN("}");
        inMainFunction = false;
        return null;
    }

    // TODO: make this handle empty expressions (should be easy)
    public Object visitGlobalVar(GlobalVar ast, Object o) {

        if (inLibCDeclarations) {
            emit("@" + ast.I.spelling + " = external global ");
            ast.T.visit(this, o);
            emitN("");
            return null;
        }

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
        String name = ast.I.spelling + depth;
        if (ast.T.isArray()) {
            emit("\t%$arr" + depth + " = alloca ");
            ast.T.visit(this, o);
            emit("\n\t%" + name + " = getelementptr inbounds ");
            ast.T.visit(this, o);
            emit(", ");
            ast.T.visit(this, o);
            emitN("* %$arr" + depth + ", i32 0, i32 0");

        } else {
            emit("\t%" + name + " = alloca ");
            ast.T.visit(this, o);
        }
        emitN("");

        if (ast.E.isNullExpr()) {
            assert(ast.T.isPointer());
            emitN("\tstore ptr null, ptr %" + name);
            return null;
        }

        if (ast.T.isArray()) {
            arrName = "$arr" + depth;
            arrayDetails = (ArrayType) ast.T;
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
                int v4 = handleSizeOfExpr(ast.T, f);
               
                emitN("\tcall void @llvm.memcpy.p0.p0.i64(ptr %" + ast.I.spelling + depth + ", ptr %" + srcIndex
                + ", i64 %" + v4 + ", i1 false)");

            }
            return null;
        } else if (ast.T.isTuple()) {
            if (ast.E.isEmptyExpr()) {
                return null;
            }

            handleBitCast(ast.T, ast.I.spelling + depth, f);
            ast.E.visit(this, o);

            if (!ast.E.isTupleExpr()) {
                int srcIndex = ast.E.tempIndex;
                int v4 = handleSizeOfExpr(ast.T, f);
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
        if (!ast.T.isPointer()) {
            emit("*");
        }
        emitN(" %" + ast.I.spelling + depth);
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
        int bV = Integer.parseInt(bottom.substring(1));
        f.setPreceding(bV);
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

        f.conStack.push(top);

        emitN("\tbr label %" + top);
        emitN("\n" + top + ":");
        ast.E.visit(this, o);
        String bottom = f.getNewLabel();
        String middle= f.getNewLabel();
        f.brkStack.push(bottom);

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
            if (inMainFunction) {
                emitN("\tret i64 0");
            } else {
                emitN("\tret void");
            }
            return null;
        }

        ast.E.visit(this, o);
        if (ast.E.type.isStruct() || ast.E.type.isTuple()) {
            int index = (ast.E.isStructExpr() || ast.E.isTupleExpr()) ? f.localVarIndex - 1 : f.localVarIndex - 2;
            if (!ast.E.isTupleExpr() && ast.E.type.isTuple()) {
                index = f.localVarIndex -1;
            }
            int v4 = handleSizeOfExpr(ast.E.type, f);
            emitN("\tcall void @llvm.memcpy.p0.p0.i64(ptr %.ret, ptr %" + index 
                + ", i64 %" + v4 + ", i1 false)");
            emitN("\tret void");
            return null;
        }

        emit("\tret ");
        int index = f.localVarIndex - 1;
        ast.E.type.visit(this, o);
        emitN(" %" + index);
        return null;
    }

    public Object visitOperator(Operator ast, Object o) {
        Frame f = (Frame) o;
        switch (ast.spelling) {
            case "i8-", "i64-", "i32-", "f32-", "f64-" -> {
                switch (ast.parent) {
                    case BinaryExpr B -> {
                        int numOne = B.E1.tempIndex;
                        int numTwo = B.E2.tempIndex;
                        int newNum = f.getNewIndex();
                        B.tempIndex = newNum;
                        emitN("\t%" + newNum + " = " +  opToCommand(ast.spelling) +
                            " %" + numOne + ", %" + numTwo);
                    }
                    case UnaryExpr U -> {
                        int numOne = U.E.tempIndex;
                        int newNum = f.getNewIndex();
                        emit("\t%" + newNum + " = " + opToCommand(ast.spelling) + " ");
                        if (U.E.type.isFloat()) {
                            emitN("0.0");
                        } else {
                            emitN("0");
                        }
                        emitN(", %" + numOne);
                        U.tempIndex = newNum;
                    }
                    default -> {}
                }
            }
            case "b!" -> {
                switch (ast.parent) {
                    case UnaryExpr U -> {
                        int numOne = U.E.tempIndex;
                        int newNum = f.getNewIndex();
                        U.tempIndex = newNum;
                        emitN("\t%" + newNum + " = " +  "xor i1 1, " + " %" + numOne);
                    }
                    default -> {}
                }
            }
            case "i8~", "i32~", "i64~", "u8~", "u32~", "u64~" -> {
                switch (ast.parent) {
                    case UnaryExpr U -> {
                        int numOne = U.E.tempIndex;
                        int newNum = f.getNewIndex();
                        U.tempIndex = newNum;
                        String type = ast.spelling.substring(0, ast.spelling.length() - 1);
                        emitN("\t%" + newNum + " = " +  "xor " + type + " %" + numOne + ", -1");
                    }
                    default -> {}
                }
            }
            default -> {
                switch (ast.parent) {
                    case BinaryExpr B -> {
                        int numOne = B.E1.tempIndex;
                        int numTwo = B.E2.tempIndex;
                        int newNum = f.getNewIndex();
                        B.tempIndex = newNum;
                        emitN("\t%" + newNum + " = " +  opToCommand(ast.spelling) +
                            " %" + numOne + ", %" + numTwo);
                    }
                    default -> {}
                }
            }
        }
        return null;
    }

    public String opToCommand(String input) {
        return switch (input)  {
            case "i8+", "u8+" ->  "add i8";
            case "i32+", "u16+" ->  "add i32";
            case "i64+", "u64+" ->  "add i64";

            case "f32+" ->  "fadd float";
            case "f64+" ->  "fadd double";
            
            case "i8&", "u8&" ->  "and i8";
            case "i32&", "u32&" ->  "and i32";
            case "i64&", "u64&" ->  "and i64";

            case "i8|", "u8|" ->  "or i8";
            case "i32|", "u32|" ->  "or i32";
            case "i64|", "u64|" ->  "or i64";

            case "i8^", "u8^" ->  "xor i8";
            case "i32^", "u32^" ->  "xor i32";
            case "i64^", "u64^" ->  "xor i64";

            case "i8<<", "u8<<" ->  "shl i8";
            case "i32<<", "u32<<" ->  "shl i32";
            case "i64<<", "u64<<" ->  "shl i64";

            case "i8>>", "u8>>" ->  "lshr i8";
            case "i32>>", "u32>>" ->  "lshr i32";
            case "i64>>", "u64>>" ->  "lshr i64";
            
            case "i8-", "u8-" ->  "sub i8";
            case "i32-", "u32-" ->  "sub i32";
            case "i64-", "u64-" ->  "sub i64";

            case "f32-" ->  "fsub float";
            case "f64-" ->  "fsub double";

            case "i8*", "u8*" -> "mul i8";
            case "i32*", "u32*" -> "mul i32";
            case "i64*", "u64*" -> "mul i64";

            case "f32*" -> "fmul float";
            case "f64*" -> "fmul double";

            case "i8%" -> "srem i8";
            case "i32%" -> "srem i32";
            case "i64%", "ii%" -> "srem i64";

            case "u8%" -> "urem i8";
            case "u32%" -> "urem i32";
            case "u64%" -> "urem i64";

            case "i8/" -> "sdiv i8";
            case "i32/" -> "sdiv i32";
            case "i64/" -> "sdiv i64";

            case "u8/" -> "udiv i8";
            case "u32/" -> "udiv i32";
            case "u64/" -> "udiv i64";

            case "f32/" -> "fdiv float";
            case "f64/" -> "fdiv double";

            case "i8==", "u8==" -> "icmp eq i8";
            case "i32==", "u32==" -> "icmp eq i32";
            case "i64==", "u64==" -> "icmp eq i64";

            case "f32==" -> "fcmp oeq float";
            case "f64==" -> "fcmp oeq double";

            case "b==" -> "icmp eq i1";
            case "b!=" -> "icmp ne i1";

            case "i8!=", "u8!=" -> "icmp ne i8";
            case "i32!=", "u32!=" -> "icmp ne i32";
            case "i64!=", "u64!=" -> "icmp ne i64";

            case "f32!=" -> "fcmp one float";
            case "f64!=" -> "fcmp one double";

            case "i8<=", "u8<=" -> "icmp sle i8";
            case "i32<=", "u32<=" -> "icmp sle i32";
            case "i64<=", "u64<=" -> "icmp sle i64";

            case "f32<=" -> "fcmp ole float";
            case "f64<=" -> "fcmp ole double";
            
            case "p<=" -> "icmp ule ptr";

            case "i8<", "u8<" -> "icmp slt i8";
            case "i32<", "u32<" -> "icmp slt i32";
            case "i64<", "u64<" -> "icmp slt i64";

            case "f32<" -> "fcmp olt float";
            case "f64<" -> "fcmp olt double";

            case "p<" -> "icmp ult ptr";

            case "i8>", "u8>" -> "icmp sgt i8";
            case "i32>", "u32>" -> "icmp sgt i32";
            case "i64>", "u64>" -> "icmp sgt i64";

            case "f32>" -> "fcmp ogt float";
            case "f64>" -> "fcmp ogt double";

            case "p>" -> "icmp ugt ptr";

            case "i8>=", "u8>=" -> "icmp sge i8";
            case "i32>=", "u32>=" -> "icmp sge i32";
            case "i64>=", "u64>=" -> "icmp sge i64";

            case "f32>=" -> "fcmp oge float";
            case "f64>=" -> "fcmp oge double";

            case "p>=" -> "icmp uge ptr";

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

        if (ast.O.spelling.equals("p==") || ast.O.spelling.equals("p!=")) {
            boolean isEq = ast.O.spelling.equals("p==");
            Expr E = null;
            if (ast.E1.isNullExpr() && ast.E2.isNullExpr()) {
                int i = f.getNewIndex();
                ast.tempIndex = i;
                if (isEq) {
                    emitN("\t%" + i + " = add i1 0, 1");
                } else {
                    emitN("\t%" + i + " = add i1 0, 0");
                }
                return null;
            } else if (ast.E1.isNullExpr()) {
                E = ast.E2;
            } else if (ast.E2.isNullExpr()) {
                E = ast.E1;
            } else {
                // They are both not null
                ast.E1.visit(this, o);
                int indexOne = ast.E1.tempIndex;
                ast.E2.visit(this, o);
                int indexTwo = ast.E2.tempIndex;
                int i = f.getNewIndex();
                ast.tempIndex = i;
                emitN("\t%" + i + " = icmp " + (isEq ? "eq" : "ne") + " i8* %" + indexOne + ", %" + indexTwo);
                return null;
            }
            E.visit(this, o);
            int index = E.tempIndex;
            int i = f.getNewIndex();
            ast.tempIndex = i;
            if (isEq) {
                emitN("\t%" + i + " = icmp eq ptr %" + index + ", null");
            } else {
                emitN("\t%" + i + " = icmp ne ptr %" + index + ", null");
            }
            return null;
        }

        if (ast.O.spelling.equals("p+")) {
            Type innerT = ((PointerType) ast.E1.type).t;
            ast.E1.visit(this, o);
            int i1 = ast.E1.tempIndex;
            ast.E2.visit(this, o);
            int i2 = ast.E2.tempIndex;
            int v = f.getNewIndex();
            ast.tempIndex = v;
            emit("\t%" + v + " = getelementptr inbounds ");
            if (ast.E1.isDerefExpr()) {
                emit("ptr");
            } else {
                innerT.visit(this, o);
            }
            emitN(", ptr %" + i1 + ", i64 %" + i2);
            return null;
        } else if (ast.O.spelling.equals("p-")) {
            Type innerT = ((PointerType) ast.E1.type).t;
            ast.E1.visit(this, o);
            int i1 = ast.E1.tempIndex;
            ast.E2.visit(this, o);
            int i2 = ast.E2.tempIndex;
            // Negate i2
            int i3 = f.getNewIndex();
            emitN("\t%" + i3 + " = sub i64 0, %" + i2);
            int v = f.getNewIndex();
            ast.tempIndex = v;
            emit("\t%" + v + " = getelementptr inbounds ");
            if (ast.E1.isDerefExpr()) {
                emit("ptr");
            } else {
                innerT.visit(this, o);
            }
            emitN(", ptr %" + i1 + ", i64 %" + i3);
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

    public Object visitU8Expr(U8Expr ast, Object o) {
        Frame f = (Frame) o;
        String value = ast.IL.spelling;
        int num = f.getNewIndex();
        emitN("\t%" + num + " = add i8 0, " + value);
        ast.tempIndex = num;
        return null;
    }

    public Object visitU32Expr(U32Expr ast, Object o) {
        Frame f = (Frame) o;
        String value = ast.IL.spelling;
        int num = f.getNewIndex();
        emitN("\t%" + num + " = add i32 0, " + value);
        ast.tempIndex = num;
        return null;
    }

    public Object visitU64Expr(U64Expr ast, Object o) {
        Frame f = (Frame) o;
        String value = ast.IL.spelling;
        int num = f.getNewIndex();
        emitN("\t%" + num + " = add i64 0, " + value);
        ast.tempIndex = num;
        return null;
    }

    public Object visitI64Type(I64Type ast, Object o) {
        emit(LLVM.I64_TYPE);
        return null;
    }

    public Object visitVariaticType(VariaticType ast, Object o) {
        emit(LLVM.VARIATIC_TYPE);
        return null;
    }
    
    public Object visitI32Type(I32Type ast, Object o) {
        emit(LLVM.I32_TYPE);
        return null;
    }

    public Object visitParaList(ParaList ast, Object o) {
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
        return null;
    }

    public Object visitEmptyParaList(EmptyParaList ast, Object o) {
        return null;
    }

    public Object visitParaDecl(ParaDecl ast, Object o) {
        if (ast.T.isArray()) {
            emit("ptr");
            if (!inLibCDeclarations) {
                emit(" %" + ast.I.spelling);
            }
            return null;
        } else {
            ast.T.visit(this, o);
            if (ast.T.isStruct() || ast.T.isTuple()) {
                emit("*");
            }
        }

        if (inLibCDeclarations) {
            return null;
        }

        if (!ast.isMut && !ast.T.isPointer() && !ast.T.isStruct() && !ast.T.isTuple()) {
            emit(" %" + ast.I.spelling + "0");
        } else {
            emit(" %" + ast.I.spelling);
        }
        ast.index = "0";
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

        switch (d) {
            case LocalVar L -> {
                int newIndex = -1;
                if (L.T.isArray()) {
                    newIndex = f.getNewIndex();
                    emitN("\t%" + newIndex + " = bitcast ptr %" + L.I.spelling + L.index + " to ptr");
                    if (inCallOrMethodAccessExpr || ast.inDeclaringLocalVar) {
                        return null;
                    }
                    newIndex = f.getNewIndex();
                } else if (L.T.isStruct() || L.T.isTuple()) {
                    newIndex = handleBitCast(L.T, ast.I.spelling + L.index, f);
                    if (!ast.inDeclaringLocalVar && !ast.inCallExpr && !L.T.isTuple()) {
                        int v2 = f.getNewIndex();
                        handleLoad(L.T, newIndex, v2, f);
                    }
                    return null;
                } else {
                    newIndex = f.getNewIndex();
                }
                handleLoad(L.T, ast.I.spelling + L.index, newIndex, f);
            }
            case ParaDecl P -> {
                if (P.T.isStruct() || P.T.isTuple()) {
                    int newIndex = handleBitCast(P.T, ast.I.spelling + 0, f);
                    if (!ast.inDeclaringLocalVar && !ast.inCallExpr && !P.T.isTuple()) {
                        int v2 = f.getNewIndex();
                        handleLoad(P.T, newIndex, v2, f);
                    }
                    return null;
                }

                if (P.isMut) {
                    int newIndex = f.getNewIndex();
                    handleLoad(P.T, ast.I.spelling + P.index, newIndex, f);
                    return null;
                }

                if (P.T.isPointer()) {
                    int newIndex = f.getNewIndex();
                    handleLoad(P.T, ast.I.spelling + P.index, newIndex, f);
                    return null;
                }

                int newIndex = f.getNewIndex();
                emit("\t%" + newIndex + " = ");
                Type T = P.T;
                if (T.isFloat()) {
                    emit("fadd ");
                    T.visit(this, o);
                    emit(" 0.0, ");
                } else if (T.isNumeric() || T.isBoolean() || T.isEnum()) {
                    emit("add ");
                    T.visit(this, o);
                    emit(" 0, ");
                }

                emitN(" %" + ast.I.spelling + "0");

            }
            case GlobalVar G -> {
                int newIndex = f.getNewIndex();
                emit("\t%" + newIndex + " = load ");
                G.T.visit(this, o);
                emit(", ");
                G.T.visit(this, o);
                if (!G.T.isPointer()) {
                    emit("*");
                }
                emitN(" @" + ast.I.spelling);
            }
            default -> {}
        }

        return null;
   }


    public Object visitCallExpr(CallExpr ast, Object o) {

        Frame f = (Frame) o;

        // Evaluate all the expressions
        inCallOrMethodAccessExpr = true;
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
        inCallOrMethodAccessExpr = false;

        Function functionRef = (Function) ast.I.decl;

        if (functionRef.T.isTuple() || functionRef.T.isStruct()) {
           int num = f.getNewIndex();
           ast.tempIndex = num; 
           emit("\t%" + num + " = alloca ");
           functionRef.T.visit(this, o);
           emit("\n\tcall void");
           f.getNewIndex();
        } else if (functionRef.T.isVoid()) {
            emit("\tcall void");
        } else {
            int num = f.getNewIndex();
            ast.tempIndex = num;
            emit("\t%" + num + " = call ");
            functionRef.T.visit(this, o);
        }
        if (ast.I.isModuleAccess) {
            Module refMod = currentModule.getModuleFromAlias(ast.I.module.get());
            String path = refMod.fileName.replace("/", ".");
            emit(" @" + path + ast.I.spelling + "." + ast.TypeDef);
        }  else if (ast.isLibC) {
            emit(" @" + ast.I.spelling);
        } else  {
            String path = functionRef.filename.replace("/", ".");
            emit(" @" + path + ast.I.spelling + "." + ast.TypeDef);
        } 
        emit("(");

        if (functionRef.T.isStruct() || functionRef.T.isTuple()) {
            emit("ptr");
            emit(" %" + ast.tempIndex);
            if (!ast.AL.isEmptyArgList()) {
                emit(", ");
            }
        }

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
                    if (E.type.isStruct() || E.type.isTuple()) {
                        emit("*");
                    }
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

    public Object visitTupleDestructureAssignStmt(TupleDestructureAssignStmt ast, Object o) {
        ast.TDA.visit(this, o);
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
        emit("ptr");
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
                if (!innerType.isPointer()) {
                    emit("*");
                }
                emitN(" %" + tempIndex);
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
                if (!innerType.isPointer()) {
                    emit("*");
                }
                emitN(" %" + tempIndex);
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

        Type t = ast.parentType;
        Type innerT = null;
        if (t.isArray()) {
            innerT = ((ArrayType) t).t;
        } else if (t.isPointer()) {
            innerT = ((PointerType) t).t;
        }
        Frame f = (Frame) o;

        int pointerV = -1;
        if (t.isPointer()) {
            pointerV = f.getNewIndex();
            if (innerT.isPointer()) {
                emit("\t%" + pointerV + " = bitcast ptr ");
                switch (ast.I.decl) {
                    case LocalVar L -> emit("%" + ast.I.spelling + L.index);
                    case ParaDecl _ -> emit("%" + ast.I.spelling);
                    default -> System.out.println("ArrayIndexExpr not implemented");
                }
                emitN(" to ptr");
            } else {
                emit("\t%" + pointerV + " = load ptr, ptr ");
                switch (ast.I.decl) {
                    case LocalVar L -> emitN("%" + ast.I.spelling + L.index);
                    case ParaDecl _ -> emitN("%" + ast.I.spelling);
                    default -> System.out.println("ArrayIndexExpr not implemented");
                }
            }
        }

        ast.index.visit(this, o);

        int indexV = f.localVarIndex - 1;
        int newV = f.getNewIndex();

        emit("\t%" + newV + " = getelementptr ");
        if (ast.I.decl instanceof GlobalVar) {
            t.visit(this, o);
            emitN(", ptr @" + ast.I.spelling + ", i64 0, i64 %" + indexV);
        } else {
            innerT.visit(this, o);
            emit(", ptr");
            if (t.isPointer()) {
                emit(" %" + pointerV);
            } else if (ast.I.decl instanceof LocalVar L) {
                emit(" %" + ast.I.spelling + L.index);
            } else if (ast.I.decl.isParaDecl()) {
                emit(" %" + ast.I.spelling);
            }
            emitN(", i64 %" + indexV);
        }

        int oldV = newV;
        if (!((innerT.isStruct() || innerT.isTuple()) && ast.inDeclaringLocalVar)) {
            newV = f.getNewIndex();

            emit("\t%" + newV + " = load ");
            innerT.visit(this, o);
            emitN(", ptr %" + oldV);
        }

        ast.tempIndex = newV;
        return null;
    }

    public Object visitI8Type(I8Type ast, Object o) {
        emit(LLVM.I8_TYPE);
        return null;
    }

    public Object visitU8Type(U8Type ast, Object o) {
        emit(LLVM.I8_TYPE);
        return null;
    }

    public Object visitU32Type(U32Type ast, Object o) {
        emit(LLVM.I32_TYPE);
        return null;
    }

    public Object visitU64Type(U64Type ast, Object o) {
        emit(LLVM.I64_TYPE);
        return null;
    }

    public Object visitCharExpr(I8Expr ast, Object o) {
        Frame f = (Frame) o;
        int v = -1;
        if (ast.CL.isPresent()) {
            String value = ast.CL.get().spelling;
            v = (int) value.charAt(0);
        } else {
            v = Integer.parseInt(ast.IL.get().spelling);
        }
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

        if (from.isVoidPointer()) {
            emit("bitcast ptr %" + numOne + " to ");
            to.visit(this, o);
            emitN("");
            return null;
        } else if (to.isVoidPointer()) {
            emit("bitcast ");
            from.visit(this, o);
            emitN(" %" + numOne + " to ptr");
            return null;
        }

        if (from.isSignedInteger() && to.isUnsignedInteger()) {
            emit("add ");
            from.visit(this, o);
            emit(" 0, %" + numOne);
            return null;
        }

        if (from.isUnsignedInteger() && to.isSignedInteger()) {
            emit("add ");
            from.visit(this, o);
            emit(" 0, %" + numOne);
            return null;
        }

        if (from.isI64() || from.isU64()) {
            switch (to) {
                case I32Type _ -> emit("trunc i64 %" + numOne + " to i32");
                case U32Type _ -> emit("trunc i64 %" + numOne + " to i32");
                case I8Type  _ -> emit("trunc i64 %" + numOne + " to i8");
                case U8Type  _ -> emit("trunc i64 %" + numOne + " to i8");
                case F32Type _ -> emit("sitofp i64 %" + numOne + " to float");
                case F64Type _ -> emit("sitofp i64 %" + numOne + " to double");
                default -> System.out.println("CastExpr not implemented");
            }
        }

        if (from.isI32() || from.isU32()) {
            switch (to) {
                case I64Type _ -> emit("sext i32 %" + numOne + " to i64");
                case U64Type _ -> emit("sext i32 %" + numOne + " to i64");
                case I8Type  _ -> emit("trunc i32 %" + numOne + " to i8");
                case U8Type  _ -> emit("trunc i32 %" + numOne + " to i8");
                case F32Type _ -> emit("sitofp i32 %" + numOne + " to float");
                case F64Type _ -> emit("sitofp i32 %" + numOne + " to double");
                default -> System.out.println("CastExpr not implemented");
            }
        }

        if (from.isI8() || from.isU8()) {
            switch (to) {
                case I64Type _ -> emit("sext i8 %" + numOne + " to i64");
                case U64Type _ -> emit("sext i8 %" + numOne + " to i64");
                case I32Type _ -> emit("sext i8 %" + numOne + " to i32");
                case U32Type _ -> emit("sext i8 %" + numOne + " to i32");
                case F32Type _ -> emit("sitofp i8 %" + numOne + " to float");
                case F64Type _ -> emit("sitofp i8 %" + numOne + " to double");
                default -> System.out.println("CastExpr not implemented");
            }
        }

        if (from.isF32()) {
            switch (to) {
                case I64Type _ -> emit("fptosi float %" + numOne + " to i64");
                case U64Type _ -> emit("fptosi float %" + numOne + " to i64");
                case I32Type _ -> emit("fptosi float %" + numOne + " to i32");
                case U32Type _ -> emit("fptosi float %" + numOne + " to i32");
                case I8Type  _ -> emit("fptosi float %" + numOne + " to i8");
                case U8Type  _ -> emit("fptosi float %" + numOne + " to i8");
                case F64Type _ -> emit("fpext float %" + numOne + " to double");
                default -> System.out.println("CastExpr not implemented");
            }
        }

        if (from.isF64()) {
            switch (to) {
                case I64Type _ -> emit("fptosi double %" + numOne + " to i64");
                case U64Type _ -> emit("fptosi double %" + numOne + " to i64");
                case I32Type _ -> emit("fptosi double %" + numOne + " to i32");
                case U32Type _ -> emit("fptosi double %" + numOne + " to i32");
                case I8Type  _ -> emit("fptosi double %" + numOne + " to i8");
                case U8Type  _ -> emit("fptosi double %" + numOne + " to i8");
                case F32Type _ -> emit("fptrunc double %" + numOne + " to float");
                default -> System.out.println("CastExpr not implemented");
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
        String path = ast.fileName.replace("/", ".");
        s = path + "." + ast.I.spelling;
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
        if ((!ast.E.isStructExpr() && ast.E.type.isStruct()) || (!ast.E.isArrayInitExpr() && ast.E.type.isArray()) || (!ast.E.isTupleExpr() && ast.E.type.isTuple())) {
            int srcIndex = ast.E.tempIndex;
            int v4 = handleSizeOfExpr(ast.E.type, f);
            emitN("\tcall void @llvm.memcpy.p0.p0.i64(ptr %" + v + ", ptr %" + srcIndex
                + ", i64 %" + v4 + ", i1 false)");
        }
        else if (!(ast.E.isStructExpr() || ast.E.isArrayInitExpr())) {
            int v2 = f.localVarIndex - 1;
            emit("\tstore ");
            ast.E.type.visit(this, o);
            emit(" %" + v2 + ", ");
            ast.E.type.visit(this, o);
            if (!ast.E.type.isPointer()) {
                emit("*");
            }
            emitN(" %" + v);
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

        int lV = -1;
        if (ast.parent.isReturnStmt()) {
            lV = f.getNewIndex();
            emitN("\t%" + lV + " = alloca %" + currentStructName.peek());
        }
        currentStructPointers.push(f.localVarIndex - 1);

        ast.SA.visit(this, o);

        if (ast.parent.isReturnStmt()) {
            int v2 = f.getNewIndex();
            emitN("\t%" + v2 + " = bitcast %" + currentStructName.peek()
                 + "* %" + lV + " to %" + currentStructName.peek()  + "*");
        }

        currentStructName.pop();
        currentStructPointers.pop();
        exprDepthValues.pop();
        return null;
    }

    public Object visitAssignmentExpr(AssignmentExpr ast, Object o) {
        Frame f = (Frame) o;

        if (!ast.RHS.isNullExpr()) {
            ast.RHS.visit(this, o);
        }

        int rhsIndex = ast.RHS.tempIndex;
        boolean isGlobal = false;
        if (!ast.LHS.isVarExpr()) {
            ast.LHS.parent = ast;
            ast.LHS.visit(this, o);
        }

        if (ast.RHS.isNullExpr()) {
            assert(ast.LHS.type.isPointer());
            String V;
            if (ast.LHS.isVarExpr()) {
                SimpleVar VS = (SimpleVar) ((VarExpr) ast.LHS).V;
                V = VS.I.spelling + ((Decl) VS.I.decl).index;
            } else {
                V = String.valueOf(f.localVarIndex - 1);
            }
            emitN("\tstore ptr null, ptr %" + V);
            return null;
        }

        int lhsIndex = f.localVarIndex - 1;
        if (ast.RHS.type.isStruct() || (ast.RHS.isArrayIndexExpr() && 
            ast.RHS.type.isStruct()) || ast.RHS.type.isTuple()) {
               
            Type t;
            if (ast.RHS.type.isStruct() || ast.RHS.type.isTuple()) {
                t = ast.RHS.type;
            } else {
                t = ((ArrayType) ast.RHS.type).t;
            }

            int sizeOfExpr = calculatingSizeOf(t, f);

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
            + ", i64 %" + sizeOfExpr + ", i1 false)");
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
        if (!ast.type.isPointer()) {
            emit("*");
        }
        if (!val.isEmpty()) {
            if (!isGlobal) {
                emit(" %");
            } else {
                emit(" @");
            }
            emitN(val);
        } else {
            emit(" %");
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
                int newV = -1;
                if (!ast.isPointerAccess) {
                    newV = f.getNewIndex();
                    emitN("\t%" + newV + " = trunc i64 %" + arrayV + " to i32");
                }

                int v = f.getNewIndex();
                Optional <StructElem> elem = null;
                if (ast.parent.isStructAccess()) {
                    Struct ref = ((StructAccess) ast.parent).ref;
                    elem = ref.getElem(ast.SA.spelling);
                } else if (ast.parent.isStructAccessList()) {
                    Struct ref = ((StructAccessList) ast.parent).ref;
                    elem = ref.getElem(ast.SA.spelling);
                }

                if (ast.isPointerAccess) {

                    emitN("\t%" + v + " = load ptr, ptr %" + prevIndex);
                    prevIndex = v;
                    v = f.getNewIndex();
                    Type innerT = ((PointerType) elem.get().T).t;
                    emit("\t%" + v + " = getelementptr ");
                    innerT.visit(this, o);
                    emitN("*, ptr %" + prevIndex + ", i64 %" + arrayV);

                } else {
                    emit("\t%" + v + " = getelementptr inbounds ");
                    elem.get().T.visit(this, o);
                    emit(", ");
                    elem.get().T.visit(this, o);
                    if (!elem.get().T.isPointer()) {
                        emit("*");
                    }
                    emitN(" %" + prevIndex + ", i32 0, i32 %" + newV);
                }

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

    public Object visitMethodAccessExpr(MethodAccessExpr ast, Object o) {
        
        Frame f = (Frame) o;

        // Evaluate all the expressions
        inCallOrMethodAccessExpr = true;
        if (!ast.args.isEmptyArgList()) {
            Args AL = (Args) ast.args;
            while (true) {
                AL.E.visit(this, o);
                if (AL.EL.isEmptyArgList()) {
                    break;
                }
                AL = (Args) AL.EL;
            }
        }
        inCallOrMethodAccessExpr = false;
        Type T = ast.type;

        int vwhat = -1;
        if (ast.refExpr != null) {
            ast.refExpr.visit(this, o);
            vwhat = f.localVarIndex - 1;
        }


        if (T.isTuple() || T.isStruct()) {
            int num = f.getNewIndex();
            ast.tempIndex = num;
            emit("\t%" + num + " = alloca ");
            T.visit(this, o);
            emit("\n\tcall void");
            f.getNewIndex();
        } else if (T.isVoid()) {
            emit("\tcall void");
        } else {
            int num = f.getNewIndex();
            ast.tempIndex = num;
            emit("\t%" + num + " = call ");
            T.visit(this, o);
        }

        String path = ast.ref.filename.replace("/", ".");
        emit(" @" + path + ast.I.spelling + "." + ast.ref.attachedStruct.T.getMini() + "." + ast.TypeDef);
        emit("(");

        if (T.isTuple() || T.isStruct()) {
            emit("ptr %" + ast.tempIndex);
            emit(", ");
        }

        if (ast.refExpr != null) {
            Type DT = ast.refExpr.type;
            DT.visit(this, o);
            emit(" %" + vwhat);
        } else if (ast.parent.isMethodAccessExpr()) {
            int v = ((MethodAccessExpr) ast.parent).tempIndex;
            ((MethodAccessExpr) ast.parent).type.visit(this, o);
            emit(" %" + v);
        } else {
            System.out.println("UNREACHABLE");
        }

        if (!ast.args.isEmptyArgList()) {
            emit(", ");
        }

        if (!ast.args.isEmptyArgList()) {
            Args AL = (Args) ast.args;
            while (true) {
                Expr E = AL.E;
                int index = E.tempIndex;
                // pass arrays by ref, not val
                if (E.type.isArray()) {
                    if (E.isArrayIndexExpr()) {
                        ((ArrayType) E.type).t.visit(this, o);
                    } else {
                        PointerType pT = new PointerType(dummyPos, ((ArrayType) E.type).t);
                        pT.visit(this, o);
                    }
                } else {
                    E.type.visit(this, o);
                    if (E.type.isStruct() || E.type.isTuple()) {
                        emit("*");
                    }
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

        ast.next.visit(this, o);
        return null;
    }

    public Object visitTupleAccess(TupleAccess ast, Object o) {
        Frame f = (Frame) o;
        TupleType ref = ast.ref;
        ast.V.visit(this, o);
        int prevIndex = f.localVarIndex - 1;
        String name = "%tuple." + ref.index;
        int newV = f.getNewIndex();
        String index = ast.index.IL.spelling;
        emit("\t%" + newV + " = getelementptr " + name + ", " + name + "* %" + prevIndex);
        emitN(", i32 0, i32 " + index);
        ast.tempIndex = newV;

        if (!ast.parent.isLocalVar() && !(ast.parent.isAssignmentExpr() && ast.isLHSOfAssignment)) {
            int oldV = f.localVarIndex - 1;
            ast.tempIndex = f.getNewIndex();
            handleLoad(ast.type, oldV, ast.tempIndex, f);
        }
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

            if (ast.isPointerAccess) {
                emitN("\t%" + v + " = load %" + structName + "*, ptr %" + localRef);
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

          if (ast.isPointerAccess) {
                emitN("\t%" + v + " = load %" + structName + "*, ptr %" + localRef);
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

    public Object visitNullExpr(NullExpr ast, Object o) {
        return null;
    }

    public Object visitDerefExpr(DerefExpr ast, Object o) {
        Frame f = (Frame) o;
        if (ast.E instanceof VarExpr V) {
            SimpleVar VS = (SimpleVar) V.V;

            String name = switch (VS.I.decl) {
                case LocalVar L -> VS.I.spelling + L.index;
                case ParaDecl _ -> VS.I.spelling + "0";
                default -> throw new RuntimeException("DerefExpr not implemented");
            };

            if (ast.parent.isAssignmentExpr() && ast.isLHSOfAssignment 
                && ast.type.isPointer()) { 
                int v = f.getNewIndex();
                emitN("\t%" + v + " = bitcast ptr %" + name + " to ptr");
                ast.tempIndex = v;
                return null;
            }
            int v = f.getNewIndex();
            ast.tempIndex = v;
            emit("\t%" + v + " = load ");
            ast.E.type.visit(this, o);
            emit(", ");
            ast.E.type.visit(this, o);
            if (!ast.E.type.isPointer()) {
                emit("*");
            }

            switch(VS.I.decl) {
                case LocalVar L -> emitN(" %" + VS.I.spelling + L.index);
                case GlobalVar _ -> emitN(" @" + VS.I.spelling);
                case ParaDecl _ -> emitN(" %" + VS.I.spelling + "0");
                default -> System.out.println("DerefExpr not implemented");
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
        switch (t) {
            case I8Type  _ -> I = new I8Expr(new CharLiteral("0", dummyPos), dummyPos);
            case I32Type _ -> I = new I32Expr(new IntLiteral("0", dummyPos), dummyPos);
            case I64Type _ -> I = new I64Expr(new IntLiteral("0", dummyPos), dummyPos);
            case F32Type _ -> I = new F32Expr(new DecimalLiteral("0.0", dummyPos), dummyPos);
            case F64Type _ -> I = new F64Expr(new DecimalLiteral("0.0", dummyPos), dummyPos);
            case BooleanType _ -> I = new BooleanExpr(new BooleanLiteral("false", dummyPos), dummyPos);
            default -> {
                I = new I64Expr(new IntLiteral("0", dummyPos), dummyPos);
                System.out.println("Not implemented");
            }
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
        if (t.isPointer()) {
            emitN(" %" + v1);
        } else {
            emitN("* %" + v1);
        }
    }

    public void handleLoad(Type t, int v1, String v2, Frame o) {
        emit("\t%" + v2 + " = load ");
        t.visit(this, o);
        emit(", ");
        t.visit(this, o);
        if (t.isPointer()) {
            emitN(" %" + v1);
        } else {
            emitN("* %" + v1);
        }
    }

    public void handleLoad(Type t, String v1, int v2, Frame o) {
        emit("\t%" + v2 + " = load ");
        t.visit(this, o);
        emit(", ");
        t.visit(this, o);
        if (t.isPointer()) {
            emitN(" %" + v1);
        } else {
            emitN("* %" + v1);
        }
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

    public Object visitTypeOfExpr(TypeOfExpr ast, Object o) {
        ast.SE.visit(this, o);
        ast.tempIndex = ast.SE.tempIndex;
        return null;
    }

    private int calculatingSizeOf(Type t, Frame f) {
        int v = f.getNewIndex();
        int v2 = f.getNewIndex();
        emit("\t%" + v + " = getelementptr ");
        t.visit(this, f);
        emit(", ");
        t.visit(this, f);
        emitN("* null, i32 1");
        emit("\t%" + v2 + " = ptrtoint ");
        t.visit(this, f);
        emitN("* %" + v + " to i64");
        return v2;
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
            return calculatingSizeOf(T, f);
        } else if (t.isTuple()) {
            TupleType T = (TupleType) t;
            return calculatingSizeOf(T, f);
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

    public Object visitUsingStmt(UsingStmt ast, Object o) {
        return null;
    }

    public Object visitIntExpr(IntExpr ast, Object o) {
        return null;
    }   

    public Object visitDecimalExpr(DecimalExpr ast, Object o) {
        return null;
    }

    public Object visitTypeList(TypeList ast, Object o) {
        ast.T.visit(this, o);
        if (ast.TL.isTypeList()) {
            emit(", ");
        }
        ast.TL.visit(this, o);
        return null;
    }

    public Object visitEmptyTypeList(EmptyTypeList ast, Object o) {
        return null;
    }

    public Object visitTupleType(TupleType ast, Object o) {
    
        if (inTupleDeclarations) {
            
            if (ast.inAnotherTupleType && inSubTuple) {
                emit("%tuple." + ast.index);
                emit(" = type { ");
                declaringTopTuple = true;
                ast.TL.visit(this, o);
                emitN("}");
                return null;
            }

            if (ast.inAnotherTupleType && !declaringTopTuple) {
                return null;
            }

            if (ast.inAnotherTupleType && !inSubTuple) {
                emit("%tuple." + ast.index);
                return null;
            }
            emit("%tuple." + ast.index);
            emit(" = type { ");
            declaringTopTuple = true;
            ast.TL.visit(this, o);
            emitN("}");
        } else {
            emit("%tuple." + ast.index);
        }

        return null;
    }

    private Stack<Integer> tupleIndex = new Stack<>();
    private Stack<TupleType> currentTupleType = new Stack<>();
    private Stack<Integer> currentTuplePointer = new Stack<>();

    public Object visitTupleExpr(TupleExpr ast, Object o) {
        TupleType T = (TupleType) ast.type;

        Frame f = (Frame) o;
        int v = -1;
        if (ast.parent.isReturnStmt()) {
            v = f.getNewIndex();
            emitN("\t%" + v + " = alloca %tuple." + T.index);
        }
        
        tupleIndex.push(0);
        currentTupleType.push(T);
        currentTuplePointer.push(((Frame) o).localVarIndex - 1);

        ast.EL.visit(this, o);

        if (ast.parent.isReturnStmt()) {
            int v2 = f.getNewIndex();
            emitN("\t%" + v2 + " = bitcast %tuple." + T.index + "* %"
                + v + " to %tuple." + T.index + "*");
        }

        tupleIndex.pop();
        currentTupleType.pop();
        currentTuplePointer.pop();

        return null;
    }

    public Object visitTupleExprList(TupleExprList ast, Object o) {
        Frame f = (Frame) o;

        int originalPointer = currentTuplePointer.peek();
        int tupleTypeIndex = currentTupleType.peek().index;
        int v = f.getNewIndex();
        emitN("\t%" + v + " = getelementptr %tuple." + tupleTypeIndex + ", %tuple." + tupleTypeIndex +
            "* %" + originalPointer + ", i32 0, i32 " + tupleIndex.peek());

        tupleIndex.push(tupleIndex.pop() + 1);

        if (ast.E.type.isArray()) {
            arrName = String.valueOf(v);
            arrayDetails = (ArrayType) ast.E.type;
        }

        ast.E.visit(this, o);
        if ((!ast.E.isStructExpr() && ast.E.type.isStruct()) || (!ast.E.isArrayInitExpr() && ast.E.type.isArray()) || (!ast.E.isTupleExpr() && ast.E.type.isTuple())) {
            int srcIndex = ast.E.tempIndex;
            int v4 = handleSizeOfExpr(ast.E.type, f);
            emitN("\tcall void @llvm.memcpy.p0.p0.i64(ptr %" + v + ", ptr %" + srcIndex
                + ", i64 %" + v4 + ", i1 false)");
        }
        else if (!(ast.E.isStructExpr() || ast.E.isArrayInitExpr() || ast.E.isTupleExpr())) {
            int v2 = f.localVarIndex - 1;
            emit("\tstore ");
            ast.E.type.visit(this, o);
            emit(" %" + v2 + ", ");
            ast.E.type.visit(this, o);
            if (!ast.E.type.isPointer()) {
                emit("*");
            }
            emitN(" %" + v);
        }

        ast.EL.visit(this, o);
        return null;
    }

    public Object visitEmptyTupleExprList(EmptyTupleExprList ast, Object o) {
        return null;
    }

    public Object visitTupleDestructureAssign(TupleDestructureAssign ast, Object o) {
        Frame f = (Frame) o;
        TupleType TT = (TupleType) ast.T;

        int index;
        if (ast.E.isTupleExpr()) {
            String n = "tuplerand." + TT.index;
            emitN("\t%" + n + "  = alloca %tuple." + TT.index);
            index = handleBitCast(ast.T, "tuplerand." + TT.index, f);
            ast.E.visit(this, o);
        } else {
            ast.E.visit(this, o);
            if (ast.E.isVarExpr()) {
                index = f.localVarIndex - 1;
            } else {
                index = f.localVarIndex - 2;
            }
        }

        IdentsList IL = (IdentsList) ast.idents;
        int i = 0;
        while (true) {

            String name = IL.I.spelling + IL.indexT;
            emit("\t%" + name + " = alloca ");
            IL.thisT.visit(this, o);
            emitN("");

            int newV = f.getNewIndex();
            emitN("\t%" + newV + " = getelementptr %tuple." + TT.index + ", %tuple." + TT.index + "* %"
                + index + ", i32 0, i32 " + i);

            if (IL.thisT.isTuple() || IL.thisT.isStruct()) {
                int v2 = handleSizeOfExpr(IL.thisT, f);
                emitN("\tcall void @llvm.memcpy.p0.p0.i64(ptr %" + name + ", ptr %" + newV
                    + ", i64 %" + v2 + ", i1 false)");
            } else {

                int oldV = newV;
                newV = f.getNewIndex();
                handleLoad(IL.thisT, oldV, newV, f);

                emit("\tstore ");
                IL.thisT.visit(this, o);
                emit(" %" + newV + ", ");
                IL.thisT.visit(this, o);
                if (!IL.thisT.isPointer()) {
                    emit("*");
                }
                emitN(" %" + name);

            }

            if (!IL.IL.isIdentsList()) {
                break;
            }
            IL = (IdentsList) IL.IL;
            i += 1;
        }
        return null;
    }

    public Object visitIdentsList(IdentsList ast, Object o) {
        return null;
    }

    public Object visitEmptyIdentsList(EmptyIdentsList ast, Object o) {
        return null;
    }

    public Object visitMethodAccessWrapper(MethodAccessWrapper ast, Object o) {
        ast.methodAccessExpr.visit(this, o);
        // Need to bind temp index to the lowest one
        MethodAccessExpr MAE = ast.methodAccessExpr;
        while (true) {
            if (MAE.next.isMethodAccessExpr()) {
                MAE = (MethodAccessExpr) MAE.next;
            } else {
                break;
            }
        }
        ast.tempIndex = MAE.tempIndex;
        return null;
    }

    public Object visitTrait(Trait ast, Object o) {
        System.out.println("TRAIT EMITTER");
        return null;
    }

    public Object visitEmptyTraitList(EmptyTraitList ast, Object o) {
        return null;
    }

    public Object visitTraitList(TraitList ast, Object o) {
        return null;
    }

    public Object visitImpl(Impl ast, Object o) {
        ast.IL.visit(this, o);
        return null;
    }

    public Object visitMethodList(MethodList ast, Object o) {
        ast.M.visit(this, o);
        ast.L.visit(this, o);
        return null;
    }

    public Object visitEmptyMethodList(EmptyMethodList ast, Object o) {
        return null;
    }

    public Object visitExtern(Extern ast, Object o) {
        return null;
    }

    public Object visitGenericType(GenericType ast, Object o) {
        System.out.println("EMITTER: GENERIC TYPE");
        return null;
    }

    public Object visitGenericTypeList(GenericTypeList ast, Object o) {
        System.out.println("EMITTER: GENERIC TYPE LIST");
        return null;
    }

    public Object visitEmptyGenericTypeList(EmptyGenericTypeList ast, Object o) {
        System.out.println("EMITTER: EMPTY GENERIC TYPE LIST");
        return null;
    }

    public Object visitGenericFunction(GenericFunction ast, Object o) {
        System.out.println("EMITTER: GENERIC FUNCTION");
        return null;
    }

    public Object visitImplementsList(ImplementsList ast, Object o) {
        System.out.println("EMITTER: IMPLEMENTS LIST");
        return null;
    }

    public Object visitEmptyImplementsList(EmptyImplementsList ast, Object o) {
        System.out.println("EMITTER: EMPTY IMPLEMENTS LIST");
        return null;
    }
}
package X.CodeGen;

import X.Evaluator.Evaluator;
import X.Nodes.*;

public class Emitter implements Visitor {

    private final String outputName;
    private int loopDepth = 0;
    private int numConstStrings = 0;

    public Emitter(String outputName) {
        this.outputName = outputName;
    }

    public final void gen(AST ast) {
        emitN("declare i32 @printf(i8*, ...)");
        emitN("declare i32 @scanf(i8*, ...)");
        emitN("@.Istr = constant [4 x i8] c\"%d\\0A\\00\"");
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
        ast.PL.visit(this, f);
        emitN(" {");
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

    public Object visitGlobalVar(GlobalVar ast, Object o) {
        Object result = Evaluator.evalExpression(ast.E);
        emit("@" + ast.I.spelling + " = global ");
        ast.T.visit(this, o);
        emitN(" " + result.toString());
        return null;
    }

    public Object visitCompoundStmt(CompoundStmt ast, Object o) {
        if (!(ast.parent instanceof  Function || ast.parent instanceof IfStmt ||
            ast.parent instanceof ElseIfStmt || ast.parent instanceof WhileStmt
            || ast.parent instanceof ForStmt || ast.parent instanceof LoopStmt))
        {
            loopDepth++;
            ast.SL.visit(this, o);
            loopDepth--;
        } else {
            ast.SL.visit(this, o);
        }
        return null;
    }

    public Object visitLocalVar(LocalVar ast, Object o) {

        Frame f = (Frame) o;
        emit("\t%" + ast.I.spelling + loopDepth + " = alloca ");
        ast.T.visit(this, o);
        emitN("");

        if (ast.E instanceof EmptyExpr) {
            // TODO: assign default values
            return null;
        }
        ast.E.visit(this, o);


        emit("\tstore ");
        ast.T.visit(this, o);
        int value = f.localVarIndex - 1;
        emit(" %" + value + ", ");
        ast.T.visit(this, o);
        emitN("* %" + ast.I.spelling + loopDepth);
        ast.index = loopDepth;
        return null;
    }

    private String trueBottom = "";

    public Object visitIfStmt(IfStmt ast, Object o) {

        Frame f = (Frame) o;
        String middle= f.getNewLabel();
        String elseC = f.getNewLabel();
        String bottom = f.getNewLabel();
        trueBottom = bottom;

        ast.E.visit(this, o);
        int index = ast.E.tempIndex;
        emitN("\tbr i1 %" + index + ", label %" + middle + ", label %" + elseC);
        emitN("\n" + middle + ":");
        loopDepth++;
        ast.S1.visit(this, o);
        loopDepth--;
        if (!ast.S1.containsExit) {
            emitN("\tbr label %" + bottom);
        }

        ast.S2.visit(this, o);
        emitN("\n" + elseC + ":");
        loopDepth++;
        ast.S3.visit(this, o);
        loopDepth--;
        emitN("\tbr label %" + bottom);

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
        loopDepth++;
        ast.S1.visit(this, o);
        loopDepth--;
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

        loopDepth++;
        ast.S1.visit(this, o);

        emitN("\tbr label %" + top);
        emitN("\n" + top + ":");
        ast.E2.visit(this, o);
        int index = ast.E2.tempIndex;
        emitN("\tbr i1 %" + index + ", label %" + middle + ", label %" + bottom);
        emitN("\n" + middle + ":");
        ast.S.visit(this, o);
        if (!ast.S.containsExit) {
            emitN("\tbr label %" + m2);
        }
        emitN("\n" + m2 + ":");
        ast.S3.visit(this, o);
        loopDepth--;
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
        loopDepth++;
        ast.S.visit(this, o);
        loopDepth--;
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

    public Object visitDeclStmt(DeclStmt ast, Object o) {
        Frame f = (Frame) o;
        ast.E.visit(this, f);
        int value = f.localVarIndex - 1;
        emit("\tstore ");
        ast.E.type.visit(this, o);
        emit(" %" + value + ", ");
        ast.E.type.visit(this, o);
        if (ast.I.decl instanceof LocalVar) {
            emitN("* %" + ast.I.spelling + ((LocalVar) ast.I.decl).index);
        } else if (ast.I.decl instanceof GlobalVar) {
            emitN("* @" + ast.I.spelling);
        }
        return null;
    }

    public Object visitOperator(Operator ast, Object o) {
        Frame f = (Frame) o;

        switch (ast.spelling) {
            case "-" -> {
                if (ast.parent instanceof BinaryExpr parent) {
                    int numOne = parent.E1.tempIndex;
                    int numTwo = parent.E2.tempIndex;
                    int newNum = f.getNewIndex();
                    parent.tempIndex = newNum;
                    emitN("\t%" + newNum + " = sub i32 %" + numOne + ", %" + numTwo);
                } else if (ast.parent instanceof UnaryExpr parent) {
                    int numOne = parent.E.tempIndex;
                    int newNum = f.getNewIndex();
                    parent.tempIndex = newNum;
                    emitN("\t%" + newNum + " = sub i32 0, %" + numOne);
                }
            }
            case "!" -> {
                if (ast.parent instanceof UnaryExpr parent) {
                    int numOne = parent.E.tempIndex;
                    int newNum = f.getNewIndex();
                    parent.tempIndex = newNum;
                    emitN("\t%" + newNum + " = " +  "xor i1 1, " + " %" + numOne);
                }
            }
            case "+", "*", "%", "/", "==", "!=", "<", "<=", ">", ">=", "&&", "||" -> {
                if (ast.parent instanceof BinaryExpr parent) {
                    int numOne = parent.E1.tempIndex;
                    int numTwo = parent.E2.tempIndex;
                    int newNum = f.getNewIndex();
                    parent.tempIndex = newNum;
                    emitN("\t%" + newNum + " = " +  opToCommand(ast.spelling) +
                        " %" + numOne + ", %" + numTwo);
                }
            }
            default -> {
                System.out.println("OPERATOR NOT IMPLEMENTED");
                System.exit(1);
            }
        }
        return null;
    }

    public String opToCommand(String input) {
        return switch (input)  {
            case "+" -> "add i32";
            case "*" -> "mul i32";
            case "%" -> "srem i32";
            case "/" -> "udiv i32";
            case "==" -> "icmp eq i32";
            case "!=" -> "icmp ne i32";
            case "<=" -> "icmp sle i32";
            case "<" -> "icmp slt i32";
            case ">" -> "icmp sgt i32";
            case ">=" -> "icmp sge i32";
            case "&&" -> "and i1";
            case "||" -> "or i1";
            default -> "";
        };
    }

    public Object visitBinaryExpr(BinaryExpr ast, Object o) {
        ast.E1.visit(this, o);
        ast.E2.visit(this, o);
        ast.O.visit(this, o);
        return null;
    }

    public Object visitUnaryExpr(UnaryExpr ast, Object o) {
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
        ast.T.visit(this, o);
        emit(" %" + ast.I.spelling + "0");
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

    // TODO: Handle global vars
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
            emit("\t%" + newIndex + " = load ");
            ((LocalVar) d).T.visit(this, o);
            emit(", ");
            ((LocalVar) d).T.visit(this, o);
            emitN("* %" + ast.I.spelling + ((LocalVar) ast.I.decl).index);
        } else if (d instanceof ParaDecl) {
            int newIndex = f.getNewIndex();
            emit("\t%" + newIndex + " = ");
            Type T = ((ParaDecl) d).T;
            if (T.isInt() || T.isBoolean()) {
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

    public void handleInInt(CallExpr ast, Object o) {
        Frame f = (Frame) o;
        Args A= (Args) ast.AL;

        StringExpr SE = (StringExpr) A.E;
        String val = SE.SL.spelling;
        System.out.println(val);
        int l = val.length() + 1; // +1 for null termination
        emitN("\t%.str" + numConstStrings + " = alloca [" + l + " x i8], align 1");
        int XnewIndex = f.getNewIndex();
        emitN("\t%" + XnewIndex + " = getelementptr [" + l + " x i8], [" + l + " x i8]* %.str" + numConstStrings + ", i32 0, i32 0");
        emitN("\tstore [" + l + " x i8] c\"" + val + "\\00\", ["+ l +" x i8]* %.str" + numConstStrings);
        emitN("\tcall i32 (i8*, ...) @printf(i8* %" + XnewIndex + ")");
        f.getNewIndex(); // balance out temp variables
        numConstStrings += 1;

        Expr secondArg = ((Args) A.EL).E;
        VarExpr VE = (VarExpr) secondArg;
        String repetition = "";
        if (((SimpleVar) VE.V).I.decl instanceof LocalVar X) {
            repetition = String.valueOf(X.index);
        }
        String varName = ((SimpleVar) VE.V).I.spelling;
        int indexStr = f.getNewIndex();
        int newIndex = f.getNewIndex();
        emitN("\t%" + indexStr + " = getelementptr inbounds [3 x i8], [3 x i8]* @.IIstr, i32 0, i32 0");
        AST X = ((SimpleVar) VE.V).I.decl;
        if (X instanceof LocalVar || X instanceof ParaDecl) {
            emitN("\t%" + newIndex + " = call i32 (i8*, ...) @scanf(i8* %" + indexStr + ", i32* %" + varName + repetition + ")");
        } else if (X instanceof GlobalVar) {
            emitN("\t%" + newIndex + " = call i32 (i8*, ...) @scanf(i8* %" + indexStr + ", i32* %" + varName + repetition + ")");
        }
    }

    public Object visitCallExpr(CallExpr ast, Object o) {

        if (ast.I.spelling.equals("outInt")) {
            handleOutInt(ast, o);
            return null;
        }
        if (ast.I.spelling.equals("inInt")) {
            handleInInt(ast, o);
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
        emit(" @" + ast.I.spelling);

        emit("(");
        if (!(ast.AL instanceof EmptyArgList)) {
            Args AL = (Args) ast.AL;
            while (true) {
                Expr E = AL.E;
                int index = E.tempIndex;
                E.type.visit(this, o);
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

    public Object visitCallStmt(CallStmt ast, Object o) {
        ast.E.visit(this, o);
        return null;
    }

    public Object visitLoopStmt(LoopStmt ast, Object o) {
        Frame f = (Frame) o;
        int newIndex;
        loopDepth++;

        String dollars;
        if (ast.varName.isPresent()) {
            dollars = ast.varName.get().I.spelling + loopDepth;
            ast.varName.get().index = loopDepth;
        } else {
            dollars = "$".repeat(f.getDollarDepth() + 1);
        }
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
            loopDepth--;
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
            loopDepth++;
            ast.S.visit(this, o);
            loopDepth--;
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

    public Object visitMathDeclStmt(MathDeclStmt ast, Object o) {

        String repetitions = "";
        if (ast.I.decl instanceof LocalVar) {
            repetitions = String.valueOf(((LocalVar) ast.I.decl).index);
        }

        Frame f = (Frame) o;
        ast.E.visit(this, o);
        int index = ast.E.tempIndex;
        int aIndex = f.getNewIndex();
        if (ast.I.decl instanceof LocalVar || ast.I.decl instanceof ParaDecl) {
            emitN("\t%" + aIndex + " = load i32, i32* %" + ast.I.spelling + repetitions);
        } else if (ast.I.decl instanceof GlobalVar) {
            emitN("\t%" + aIndex + " = load i32, i32* @" + ast.I.spelling + repetitions);
        }
        int nIndex = f.getNewIndex();
        switch (ast.O.spelling) {
            case "+=" -> {
                emitN("\t%" + nIndex + " = add i32 %" + index + ", %" + aIndex);
            }
            case "-=" -> {
                emitN("\t%" + nIndex + " = sub i32 %" + index + ", %" + aIndex);
            }
            case "*=" -> {
                emitN("\t%" + nIndex + " = mul i32 %" + index + ", %" + aIndex);
            }
            case "/=" -> {
                emitN("\t%" + nIndex + " = udiv i32 %" + index + ", %" + aIndex);
            }
            default -> {
                System.out.println("UNREACHABLE MATHDECL");
            }
        }
        if (ast.I.decl instanceof LocalVar || ast.I.decl instanceof ParaDecl) {
            emitN("\tstore i32 %" + nIndex + ", i32* %" + ast.I.spelling + repetitions);
        } else if (ast.I.decl instanceof GlobalVar) {
            emitN("\tstore i32 %" + nIndex + ", i32* @" + ast.I.spelling + repetitions);
        }
        return null;
    }

    public void emit(String s) {
        LLVM.append(new Instruction(s));
    }

    public void emitN(String s) {
        LLVM.append(new Instruction(s + "\n"));
    }
}


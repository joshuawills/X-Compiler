package X.Checker;

import X.Environment;
import X.ErrorHandler;
import X.Lexer.Position;
import X.Nodes.*;

public class Checker implements Visitor {

    private final String[] errors = {
        "*0: main function is missing",
        "*1: return type of 'main' is not int",
        "*2: identifier redeclared",
        "*3: identifier declared void",
        "*4: identifier undeclared",
        "*5: incompatible type for assignment",
        "*6: incompatible type for return",
        "*7: incompatible type for this binary operator",
        "*8: incompatible type for this unary operator",
        "*9: attempt to use an function as a scalar",
        "*10: attempt to reference a scalar as a function",
        "*11: if conditional is not boolean",
        "*12: for conditional is not boolean",
        "*13: while conditional is not boolean",
        "*14: break must be in a while/for",
        "*15: continue must be in a while/for",
        "*16: too many actual parameters",
        "*17: too few actual parameters",
        "*18: wrong type for actual parameter",
        "*19: main function may not have any parameters",
        "*20: main function may not call itself",
        "*21: statement(s) not reached",
        "*22: missing return statement",
        "*23: attempting to redeclare a constant",
        "*24: variable declared but never used",
        "*25: variable declared mutable but never reassigned",
        "*26: function declared but never used",
        "*27: inappropriate use of '$' operator",
        "*28: loop iterators must be integers",
        "*29: do-while conditional is not boolean",
    };

    private final SymbolTable idTable;
    private final ErrorHandler handler;

    private boolean hasMain = false;
    private boolean hasReturn = false;
    private boolean inMain = false;
    private int loopDepth = 0;
    private int loopKDepth = 0;
    private boolean validDollar = false;
    private Type currentFunctionType = null;

    private final Position dummyPos = new Position();

    public Checker(ErrorHandler handler) {
        this.handler = handler;
        this.idTable = new SymbolTable();
        establishEnv();
    }

    public void check(AST ast) {

        // Load in function names and global vars
        DeclList L = (DeclList) ((Program) ast).PL;
        while (true) {
            if (L.D instanceof GlobalVar V) {
                visitVarDecl(V, V.T, V.I, V.E);
            } else if (L.D instanceof Function F) {
                Decl e = idTable.retrieve(F.I.spelling);
                if (e != null) {
                    String message = String.format("'%s'. Previously declared at line %d", F.I.spelling,
                            e.pos.lineStart);
                    handler.reportError(errors[2] + ": %", message, F.I.pos);
                }
                stdFunction(F);
            }
            if (L.DL instanceof EmptyDeclList) {
                break;
            }
            L = (DeclList) L.DL;
        }
        ast.visit(this, null);

        L = (DeclList) ((Program) ast).PL;
        while (true) {
            if (L.D instanceof GlobalVar V) {
                if (!V.isUsed) {
                    String message = "'" + V.I.spelling + "'";
                    handler.reportMinorError(errors[24] + ": %", message, V.pos);
                }
                if (V.isMut && !V.isReassigned) {
                    String message = "'" + V.I.spelling + "'";
                    handler.reportMinorError(errors[25] + ": %", message, V.pos);
                }
            } else if (L.D instanceof Function F) {
                if (!F.isUsed && !F.I.spelling.equals("main")) {
                    String message = "'" + F.I.spelling + "'";
                    handler.reportMinorError(errors[26] + ": %", message, F.pos);
                }
            }
            if (L.DL instanceof EmptyDeclList) {
                break;
            }
            L = (DeclList) L.DL;
        }

        if (!hasMain) {
            handler.reportError(errors[0], "", ast.pos);
        }
    }

    private void establishEnv() {
        Ident i = new Ident("x", dummyPos);
        Environment.booleanType = new BooleanType(dummyPos);
        Environment.intType = new IntType(dummyPos);
        Environment.strType = new StringType(dummyPos);
        Environment.voidType = new VoidType(dummyPos);
        Environment.errorType = new ErrorType(dummyPos);
        Environment.inInt = stdFunction(Environment.voidType, "inInt", new ParaList(
            new ParaDecl(Environment.strType, i, dummyPos, false),
            new ParaList(
                    new ParaDecl(Environment.intType, i, dummyPos, false),
                    new EmptyParaList(dummyPos), dummyPos
            ),
            dummyPos
        ));
        Environment.outInt = stdFunction(Environment.voidType, "outInt", new ParaList(
                new ParaDecl(Environment.intType, i, dummyPos, false),
                new EmptyParaList(dummyPos), dummyPos
        ));
        Environment.outStr = stdFunction(Environment.voidType, "outStr", new ParaList(
                new ParaDecl(Environment.strType, i, dummyPos, false),
                new EmptyParaList(dummyPos), dummyPos
        ));
    }

    private Function stdFunction(Type resultType, String id, List pl) {
        Function binding = new Function(resultType, new Ident(id, dummyPos),
            pl, new EmptyStmt(dummyPos), dummyPos);
        idTable.insert(id, false, binding);
        return binding;
    }

    private void stdFunction(Function funct) {
        idTable.insert(funct.I.spelling, funct.isMut, funct);
    }

    public Object visitProgram(Program ast, Object o) {
        ast.PL.visit(this, null);
        return null;
    }

    public Object visitIdent(Ident ast, Object o) {
        Decl binding = idTable.retrieve(ast.spelling);
        if (binding != null) {
            ast.decl = binding;
        }
        return binding;
    }

    public Object visitFunction(Function ast, Object o) {
        // Check if func already exists with that name
        this.currentFunctionType = ast.T;
        if (ast.I.spelling.equals("main")) {
            inMain = hasMain = true;
            if (!ast.T.isInt()) {
                String message = "set to " + ast.T.toString();
                handler.reportError(errors[1] + ": %", message, ast.I.pos);
            }
            if (!(ast.PL instanceof EmptyParaList)) {
               handler.reportError(errors[19], "", ast.I.pos);
            }
        } else if (ast.I.spelling.equals("$")) {
            handler.reportError(errors[27] + ": %", "can't be used as function name", ast.I.pos);
        }
        idTable.openScope();
        ast.PL.visit(this, null);
        ast.S.visit(this, ast);
        idTable.closeScope();
        if (!hasReturn && !ast.T.isVoid()) {
            if (!ast.I.spelling.equals("main")) {
                handler.reportError(errors[22], "", ast.I.pos);
            }
        }

        this.currentFunctionType = null;
        inMain = hasReturn = false;
        return ast.T;
    }

    public Object visitGlobalVar(GlobalVar ast, Object o) {
        return null;
    }

    private Object visitVarDecl(Decl ast, Type T, Ident I, Expr E) {
        declareVariable(ast.I, ast);
        if (T.isVoid()) {
            handler.reportError(errors[3] + ": %", ast.I.spelling, ast.T.pos);
            T = Environment.errorType;
            return T;
        }
        I.visit(this, ast);

        if (E instanceof EmptyExpr) {
            return T;
        }

        Type returnType = (Type) E.visit(this, ast);
        if (!T.assignable(returnType) && !returnType.isError()) {
            String message = "expected " + T + ", received " + returnType;
            handler.reportError(errors[5] + ": %", message, E.pos);
            T = Environment.errorType;
        }
        return T;
    }

    public Object visitCompoundStmt(CompoundStmt ast, Object o) {
        ast.SL.visit(this, o);

        List S = ast.SL;
        while (true) {
           if (S instanceof EmptyStmtList) {
                break;
           }
           StmtList SL = (StmtList) S;
           if (SL.S instanceof LocalVarStmt) {
               LocalVar V = ((LocalVarStmt) SL.S).V;
               if (!V.isUsed) {
                   String message = "'" + V.I.spelling + "'";
                   handler.reportMinorError(errors[24] + ": %", message, V.pos);
               }
               if (V.isMut && !V.isReassigned) {
                   String message = "'" + V.I.spelling + "'";
                   handler.reportMinorError(errors[25] + ": %", message, V.pos);
               }
           }

           S = SL.SL;
        }

        ast.containsExit = ast.SL.containsExit;
        return null;
    }

    public Object visitLocalVar(LocalVar ast, Object o) {
        ast.T = (Type) visitVarDecl(ast, ast.T, ast.I, ast.E);
        return ast.T;
    }

    public Object visitIfStmt(IfStmt ast, Object o) {
        Type condT = (Type) ast.E.visit(this, ast);
        if (!condT.isBoolean()) {
            handler.reportError(errors[11], "", ast.E.pos);
        }

        idTable.openScope();
        ast.S1.visit(this, ast);
        idTable.closeScope();

        idTable.openScope();
        ast.S2.visit(this, ast);
        idTable.closeScope();

        idTable.openScope();
        ast.S3.visit(this, ast);
        idTable.closeScope();

        return null;
    }

    public Object visitElseIfStmt(ElseIfStmt ast, Object o) {
        Type condT = (Type) ast.E.visit(this, ast);
        if (!condT.isBoolean()) {
            handler.reportError(errors[11], "", ast.E.pos);
        }

        idTable.openScope();
        ast.S1.visit(this, ast);
        idTable.closeScope();

        idTable.openScope();
        ast.S2.visit(this, ast);
        idTable.closeScope();

        return null;
    }

    public Object visitForStmt(ForStmt ast, Object o) {
        idTable.openScope();
        if (!(ast.S1 instanceof EmptyStmt)) {
            ast.S1.visit(this, ast);
        }

        if (!(ast.E2 instanceof EmptyExpr)) {
            Type conditionType = (Type) ast.E2.visit(this, ast);
            if (!conditionType.isBoolean()) {
                handler.reportError(errors[12], "", ast.E2.pos);
            }
        }
        if (!(ast.S3 instanceof EmptyStmt)) {
            ast.S3.visit(this, ast);
        }
        this.loopDepth++;
        ast.S.visit(this, ast);
        idTable.closeScope();
        this.loopDepth--;
        return null;
    }

    public Object visitWhileStmt(WhileStmt ast, Object o) {
        Type conditionType = (Type) ast.E.visit(this, ast);
        if (!conditionType.isBoolean()) {
            handler.reportError(errors[13], "", ast.E.pos);
        }
        this.loopDepth++;
        idTable.openScope();
        ast.S.visit(this, ast);
        idTable.closeScope();
        this.loopDepth--;
        return null;
    }

    public Object visitBreakStmt(BreakStmt ast, Object o) {
        if (this.loopDepth <= 0) {
            handler.reportError(errors[14], "", ast.pos);
        }
        ast.containsExit = true;
        if (ast.parent instanceof Stmt) {
            ((Stmt) ast.parent).containsExit = true;
        } else if (ast.parent instanceof List) {
            ((List) ast.parent).containsExit = true;
        }
        return null;
    }

    public Object visitContinueStmt(ContinueStmt ast, Object o) {
        if (this.loopDepth <= 0) {
            handler.reportError(errors[15], "", ast.pos);
        }
        ast.containsExit = true;
        if (ast.parent instanceof Stmt) {
            ((Stmt) ast.parent).containsExit = true;
        } else if (ast.parent instanceof List) {
            ((List) ast.parent).containsExit = true;
        }
        return null;
    }

    public Object visitReturnStmt(ReturnStmt ast, Object o) {
        this.hasReturn = true;

        // Returning nothing but there's something to return
        if (ast.E instanceof EmptyExpr && !(this.currentFunctionType.isVoid())) {
            String message = "expected " + this.currentFunctionType.toString() + ", received void";
            handler.reportError(errors[6] + ": %", message, ast.E.pos);
        }
        Type conditionType;
        if (ast.E instanceof EmptyExpr) {
            conditionType = new VoidType(new Position());
            ast.E.type = Environment.voidType;
        } else {
            conditionType = (Type) ast.E.visit(this, ast);
        }
        if (!this.currentFunctionType.assignable(conditionType)) {
            String message = "expected " + this.currentFunctionType.toString() +
                ", received " + conditionType.toString();
            handler.reportError(errors[6] + ": %", message, ast.E.pos);
        }

        ast.containsExit = true;
        if (ast.parent instanceof Stmt) {
            ((Stmt) ast.parent).containsExit = true;
        } else if (ast.parent instanceof List) {
            ((List) ast.parent).containsExit = true;
        } else {
            System.out.println("WHAT");
        }

        return null;
    }

    public Object visitDeclStmt(DeclStmt ast, Object o) {

        Decl decl = idTable.retrieve(ast.I.spelling);
        if (decl == null) {
            handler.reportError(errors[4] + ": %", ast.I.spelling, ast.E.pos);
            return null;
        }

        if (decl instanceof LocalVar) {
            decl.isReassigned = true;
        } else if (decl instanceof GlobalVar) {
            decl.isReassigned = true;
        }

        ast.I.decl = decl;

        if (decl instanceof Function) {
            handler.reportError(errors[9], "", ast.E.pos);
            return null;
        }

        if (!decl.isMut) {
            handler.reportError(errors[23], "", ast.E.pos);
            return null;
        }

        Type t = (Type) ast.E.visit(this, o);
        if (!decl.T.assignable(t)) {
            String message = "expected " + decl.T.toString() + ", received " + t.toString();
            handler.reportError(errors[5] + ": %", message, ast.E.pos);
        }

        return null;
    }

    public Object visitOperator(Operator ast, Object o) {
        return null;
    }

    public Object visitBinaryExpr(BinaryExpr ast, Object o) {

        ast.O.visit(this, ast);
        Type t1 = (Type) ast.E1.visit(this, ast);
        Type t2 = (Type) ast.E2.visit(this, ast);
        switch (ast.O.spelling) {
            case "||", "&&" -> {
                if (!t1.isBoolean() && !t1.isError()) {
                    handler.reportError(errors[7], "", t1.pos);
                    ast.type = Environment.errorType;
                } else if (!t2.isBoolean() && !t2.isError()) {
                    handler.reportError(errors[7], "", t2.pos);
                    ast.type = Environment.errorType;
                } else {
                    ast.type = Environment.booleanType;
                }
            }
            case "==", "!=" -> {
                if (t1.isBoolean() && t2.isBoolean()) {
                    ast.type = Environment.booleanType;
                    break;
                }
                boolean v1 = t1.isInt() || t1.isError();
                boolean v2 = t2.isInt() || t2.isError();
                if (!v1 || !v2) {
                    handler.reportError(errors[7], "", t2.pos);
                    ast.type = Environment.errorType;
                    break;
                }
                if (t1.isError() || t2.isError()) {
                    break;
                }
                ast.type = Environment.booleanType;
            }
            case "<", "<=", ">", ">=" -> {
                boolean v1 = t1.isInt() || t1.isError();
                boolean v2 = t2.isInt() || t2.isError();
                if (!v1 || !v2) {
                    handler.reportError(errors[7], "", t2.pos);
                    ast.type = Environment.errorType;
                    break;
                }
                if (t1.isError() || t2.isError()) {
                    break;
                }
                ast.type = Environment.booleanType;
            }
            case "+", "-", "/", "*", "%" -> {
                if (!t1.isInt() || !t2.isInt()) {
                    handler.reportError(errors[7], "", t1.pos);
                    ast.type = Environment.errorType;
                    break;
                }
                ast.type = Environment.intType;
            }
        }
        return ast.type;
    }

    public Object visitUnaryExpr(UnaryExpr ast, Object o) {
        ast.O.visit(this, ast);
        Type eT = (Type) ast.E.visit(this, ast);
        switch (ast.O.spelling) {
            case "+", "-" -> {
                if (eT.isError()) {
                    break;
                }
                if (!eT.isInt()) {
                    handler.reportError(errors[8], "", ast.O.pos);
                    ast.type = Environment.errorType;
                    break;
                }
                ast.type = eT;
            }
            case "!" -> {
                if (eT.isError()) {
                    break;
                }
                if (!eT.isBoolean()) {
                    handler.reportError(errors[8], "", ast.O.pos);
                    ast.type = Environment.errorType;
                    break;
                }
                ast.type = eT;
            }
        }
        return ast.type;
    }

    public Object visitEmptyExpr(EmptyExpr ast, Object o) {
        return null;
    }

    public Object visitEmptyStmt(EmptyStmt ast, Object o) {
        return null;
    }

    public Object visitDeclList(DeclList ast, Object o) {
        ast.D.visit(this, null);
        ast.DL.visit(this, null);
        return null;
    }

    public Object visitStmtList(StmtList ast, Object o) {
        if (ast.S instanceof CompoundStmt) {
            idTable.openScope();
            ast.S.visit(this, o);
            idTable.closeScope();
        } else {
            ast.S.visit(this, o);
        }
        // Unreached statements
        if (ast.S instanceof ReturnStmt && ast.SL instanceof StmtList) {
            handler.reportMinorError(errors[21], "", ast.SL.pos);
        }
        ast.SL.visit(this, o);
        return null;
    }

    public Object visitEmptyStmtList(EmptyStmtList ast, Object o) {
        return null;
    }

    public Object visitBooleanExpr(BooleanExpr ast, Object o) {
        ast.type = Environment.booleanType;
        return ast.type;
    }

    public Object visitBooleanLiteral(BooleanLiteral ast, Object o) {
        return Environment.booleanType;
    }

    public Object visitBooleanType(BooleanType ast, Object o) {
        return Environment.booleanType;
    }

    public Object visitVoidType(VoidType ast, Object o) {
        return Environment.voidType;
    }

    public Object visitErrorType(ErrorType ast, Object o) {
        return Environment.errorType;
    }

    public Object visitIntExpr(IntExpr ast, Object o) {
        ast.type = Environment.intType;
        return ast.type;
    }

    public Object visitIntLiteral(IntLiteral ast, Object o) {
        return Environment.intType;
    }

    public Object visitIntType(IntType ast, Object o) {
        return Environment.intType;
    }

    public Object visitArgList(Args ast, Object o) {
        Type realType = (Type) ast.E.visit(this, null);

        List PL = (List) o;
        // Too many params
        if (PL instanceof EmptyParaList) {
            handler.reportError(errors[16], "", ast.E.pos);
            return Environment.errorType;
        }

        Type expectedType = ((ParaList) PL).P.T;
        if (expectedType.isVoid() || !expectedType.assignable(realType)) {
            handler.reportError(errors[18], "", ast.E.pos);
        }

        ast.EL.visit(this, ((ParaList) PL).PL);
        return null;
    }

    public Object visitParaList(ParaList ast, Object o) {
        ast.P.visit(this, null);
        ast.PL.visit(this, null);
        return null;
    }

    public Object visitStringExpr(StringExpr ast, Object o) {
        ast.type = Environment.strType;
        return ast.type;
    }

    public Object visitStringLiteral(StringLiteral ast, Object o) {
        return Environment.strType;
    }

    public Object visitStringType(StringType ast, Object o) {
        return Environment.strType;
    }

    public Object visitEmptyParaList(EmptyParaList ast, Object o) {
        return null;
    }

    public Object visitParaDecl(ParaDecl ast, Object o) {
        declareVariable(ast.I, ast);
        if (ast.T.isVoid()) {
            handler.reportError(errors[3] + ": %", ast.I.spelling, ast.I.pos);
        }
        return null;
    }

    private void declareVariable(Ident ident, Decl decl) {
        if (ident.spelling.equals("$")) {
            handler.reportError(errors[27] +  ": %", "Can't use '$' operator as variable", ident.pos);
        }
        IdEntry entry = idTable.retrieveOneLevel(ident.spelling);
        if (entry != null) {
            String message = String.format("'%s'. Previously declared at line %d", ident.spelling,
                entry.attr.pos.lineStart);
            handler.reportError(errors[2] +": %", message, ident.pos);
        }
        idTable.insert(ident.spelling, decl.isMut, decl);
        ident.visit(this, null);
    }

    public Object visitEmptyCompStmt(EmptyCompStmt ast, Object o) {
        return null;
    }

    public Object visitEmptyDeclList(EmptyDeclList ast, Object o) {
        return null;
    }

    public Object visitVarExpr(VarExpr ast, Object o) {
        ast.type = (Type) ast.V.visit(this, o);
        return ast.type;
    }

    public Object visitSimpleVar(SimpleVar ast, Object o) {

        if (ast.I.spelling.equals("$") && !validDollar) {
            handler.reportError(errors[27] + ": %", ast.I.spelling, ast.I.pos);
            return Environment.errorType;
        } else if (ast.I.spelling.equals("$")) {
            return Environment.intType;
        }

        Decl decl = idTable.retrieve(ast.I.spelling);
        if (decl == null) {
            handler.reportError(errors[4] + ": %", ast.I.spelling, ast.I.pos);
            return Environment.errorType;
        }

        if (decl instanceof Function) {
            handler.reportError(errors[9], "", ast.I.pos);
            return Environment.errorType;
        } else if (decl instanceof GlobalVar || decl instanceof LocalVar) {
            decl.isUsed = true;
        }

        ast.I.decl = decl;
        return decl.T;
    }


    public Object visitCallStmt(CallStmt ast, Object o) {
        ast.E.visit(this, o);
        return null;
    }

    public Object visitLoopStmt(LoopStmt ast, Object o) {
        Type T1, T2;

        idTable.openScope();
        ast.varName.ifPresent(localVar -> declareVariable(localVar.I, localVar));
        validDollar = !ast.varName.isPresent();
        if (ast.I1.isPresent()) {
            T1 = (Type) ast.I1.get().visit(this, o);
            if (!T1.isInt()) {
                handler.reportError(errors[28], "", ast.pos);
            }
        }
        if (ast.I2.isPresent()) {
            T2 = (Type) ast.I2.get().visit(this, o);
            if (!T2.isInt()) {
                handler.reportError(errors[28], "", ast.pos);
            }
        }

        loopKDepth += 1;
        loopDepth++;
        ast.S.visit(this, o);
        loopKDepth -= 1;
        loopDepth--;
        idTable.closeScope();
        validDollar = false;
        return null;
    }

    public Object visitMathDeclStmt(MathDeclStmt ast, Object o) {
        Decl decl = idTable.retrieve(ast.I.spelling);
        if (decl == null) {
            handler.reportError(errors[4] + ": %", ast.I.spelling, ast.E.pos);
            return null;
        }

        if (!decl.T.isInt()) {
            handler.reportError(errors[5] + ": %", ast.I.spelling + ", must be an int", ast.E.pos);
            return null;
        }

        if (decl instanceof LocalVar) {
            decl.isReassigned = true;
        } else if (decl instanceof GlobalVar) {
            decl.isReassigned = true;
        }

        ast.I.decl = decl;

        if (decl instanceof Function) {
            handler.reportError(errors[9], "", ast.E.pos);
            return null;
        }

        if (!decl.isMut) {
            handler.reportError(errors[23], "", ast.E.pos);
            return null;
        }

        Type t = (Type) ast.E.visit(this, o);
        if (!decl.T.assignable(t)) {
            String message = "expected " + decl.T.toString() + ", received " + t.toString();
            handler.reportError(errors[5] + ": %", message, ast.E.pos);
        }

        return null;
    }

    public Object visitDoWhileStmt(DoWhileStmt ast, Object o) {
        this.loopDepth++;
        idTable.openScope();
        ast.S.visit(this, ast);
        idTable.closeScope();
        this.loopDepth--;
        Type conditionType = (Type) ast.E.visit(this, ast);
        if (!conditionType.isBoolean()) {
            handler.reportError(errors[29], "", ast.E.pos);
        }
        return null;
    }

    public Object visitFloatLiteral(FloatLiteral ast, Object o) {
        System.out.println("visitFloatLiteral");
        return null;
    }

    public Object visitFloatType(FloatType ast, Object o) {
        System.out.println("visitFloatType");
        return null;
    }

    public Object visitFloatExpr(FloatExpr ast, Object o) {
        System.out.println("visitFloatExpr");
        return null;
    }

    public Object visitCallExpr(CallExpr ast, Object o) {
        if (inMain && ast.I.spelling.equals("main")) {
            handler.reportError(errors[20], "", ast.I.pos);
            return Environment.errorType;
        }

        if (ast.I.spelling.equals("inInt")) {
            Args A= (Args) ast.AL;
            Expr secondArg = ((Args) A.EL).E;
            if (!(secondArg instanceof VarExpr)) {
                handler.reportError(errors[18] + ": %", "Second arg for 'inInt' must be a declared int variable", ast.I.pos);
                return Environment.errorType;
            }
            VarExpr VE = (VarExpr) secondArg;
            Decl x = idTable.retrieve(((SimpleVar) VE.V).I.spelling);
            if (x == null) {
                handler.reportError(errors[4] + ": %", ((SimpleVar) VE.V).I.spelling, ast.I.pos);
                return null;
            }
            x.isReassigned = true;
            if (!x.isMut) {
                handler.reportError(errors[23] + ": %", ((SimpleVar) VE.V).I.spelling, ast.I.pos);
                return null;
            }
        }

        // Check function exists
        Decl type = (Decl) ast.I.visit(this, null);
        if (type == null) {
            handler.reportError(errors[4] + ": %", ast.I.spelling, ast.I.pos);
            return Environment.errorType;
        }

        // Check function is actually a function
        if (!(type instanceof Function function)) {
            handler.reportError(errors[10], "", ast.I.pos);
            return Environment.errorType;
        } else {
            Decl x = idTable.retrieve(ast.I.spelling);
            ((Function) x).setUsed();
        }
        ast.AL.visit(this, function.PL);
        ast.type = type.T;
        return type.T;
    }

    public Object visitEmptyArgList(EmptyArgList ast, Object o) {
        List PL = (List) o;
        if (!(PL instanceof EmptyParaList)) {
            handler.reportError(errors[17], "", ast.pos);
        }
        return null;
    }

    public Object visitLocalVarStmt(LocalVarStmt ast, Object o) {
        ast.V.visit(this, o);
        return null;
    }
}

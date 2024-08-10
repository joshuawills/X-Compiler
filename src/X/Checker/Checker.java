package X.Checker;

import X.Environment;
import X.ErrorHandler;
import X.Lexer.Position;
import X.Nodes.*;
import X.Nodes.Enum;
import java.util.ArrayList;

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
        "*30: address-of operand only applicable to variables",
        "*31: can't get address of a constant variable",
        "*32: inappropriate deference of variable",
        "*33: identifier declared void[]",
        "*34: attempt to use an array as a scalar",
        "*35: attempt to use a scalar/function as an array",
        "*36: wrong type for element in array initializer",
        "*37: unknown array size at compile time",
        "*38: excess elements in array initializer",
        "*39: attempted reassignment of array",
        "*40: array index is not an integer",
        "*41: char expr greater than one character",
        "*42: enum declared but never used",
        "*43: unknown type",
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

        // Load  in all unique types
        DeclList L = (DeclList) ((Program) ast).PL;
        while (true) {
            if (L.D instanceof Enum E) {
                idTable.insert(E.I.spelling, E.isMut, E);
            }
            if (L.DL instanceof EmptyDeclList) {
                break;
            }
            L = (DeclList) L.DL;
        }

        // Load in function names and global vars
        L = (DeclList) ((Program) ast).PL;
        while (true) {
            if (L.D instanceof GlobalVar V) {
                visitVarDecl(V, V.T, V.I, V.E);
            } else if (L.D instanceof Function F) {
                List P = F.PL;

                // Recalculating params for abstract types
                if (!(P instanceof EmptyParaList)) {
                    while (true) {
                        ParaDecl PE = ((ParaList) P).P;
                        Type T = PE.T;
                        if (T.isMurky()) {
                            String S = ((MurkyType) T).V;
                            Decl D = idTable.retrieve(S);
                            if (!(D instanceof Enum)) {
                                handler.reportError(errors[43] + ": %", "'" + S + "'", T.pos);
                                T = Environment.errorType;
                            }
                            PE.T = new EnumType((Enum) D, D.pos);
                            PE.T.parent = ast;
                        }
                        if (((ParaList) P).PL instanceof EmptyParaList) {
                            break;
                        }
                        P = ((ParaList) P).PL;
                    }
                }

                if (F.T.isMurky()) {
                    String S = ((MurkyType) F.T).V;
                    Decl D = idTable.retrieve(S);
                    if (!(D instanceof Enum)) {
                        handler.reportError(errors[43] + ": %", "'" + S + "'", F.T.pos);
                        F.T = Environment.errorType;
                    }
                    F.T = new EnumType((Enum) D, D.pos);
                    F.T.parent = ast;
                }

                Decl e = idTable.retrieve(F.I.spelling);
                if (e != null) {
                    String tOne = F.TypeDef;
                    String tTwo = ((Function) e).TypeDef;
                    if (tOne.equals(tTwo)) {
                        String message = String.format("'%s'. Previously declared at line %d", F.I.spelling,
                                e.pos.lineStart);
                        handler.reportError(errors[2] + ": %", message, F.I.pos);
                    }
                }

                F.setTypeDef();
                stdFunction(F);
            }
            if (L.DL instanceof EmptyDeclList) {
                break;
            }
            L = (DeclList) L.DL;
        }

        // Actually visiting everything
        ast.visit(this, null);

        // Checking use of variables/reassignment
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
            } else if (L.D instanceof Enum E) {
                if (!E.isUsed) {
                    String message = "'" + E.I.spelling + "'";
                    handler.reportMinorError(errors[42] + ": %", message, E.pos);
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
        Environment.charType= new CharType(dummyPos);
        Environment.intType = new IntType(dummyPos);
        Environment.floatType = new FloatType(dummyPos);
        Environment.voidType = new VoidType(dummyPos);
        Environment.errorType = new ErrorType(dummyPos);
        Environment.outInt = stdFunction(Environment.voidType, "outInt", new ParaList(
                new ParaDecl(Environment.intType, i, dummyPos, false),
                new EmptyParaList(dummyPos), dummyPos
        ));
        Environment.outChar = stdFunction(Environment.voidType, "outChar", new ParaList(
                new ParaDecl(Environment.charType, i, dummyPos, false),
                new EmptyParaList(dummyPos), dummyPos
        ));
        Environment.outFloat = stdFunction(Environment.voidType, "outFloat", new ParaList(
                new ParaDecl(Environment.floatType, i, dummyPos, false),
                new EmptyParaList(dummyPos), dummyPos
        ));
   }

    private Function stdFunction(Type resultType, String id, List pl) {
        Function binding = new Function(resultType, new Ident(id, dummyPos),
            pl, new EmptyStmt(dummyPos), dummyPos);
        idTable.insert(id + "." + binding.TypeDef, false, binding);
        return binding;
    }

    private void stdFunction(Function funct) {
        idTable.insert(funct.I.spelling + "." + funct.TypeDef, funct.isMut, funct);
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

    private void unMurk(Decl ast) {
        String S = ((MurkyType) ast.T).V;
        Decl D = idTable.retrieve(S);
        if (!(D instanceof Enum)) {
            handler.reportError(errors[43] + ": %", "'" + S + "'", ast.T.pos);
            ast.T = Environment.errorType;
        }
        ast.T = new EnumType((Enum) D, D.pos);
        ast.T.parent = ast;
    }

    private Object visitVarDecl(Decl ast, Type T, Ident I, Expr E) {
        if (T.isMurky()) {
            unMurk(ast);
        }

        declareVariable(ast.I, ast);

        if (T.isVoid()) {
            handler.reportError(errors[3] + ": %", ast.I.spelling, ast.T.pos);
            T = Environment.errorType;
            return T;
        }

        if (T.isArray()) {
            Type iT = ((ArrayType) T).t;
            if (iT instanceof VoidType) {
                handler.reportError(errors[33] + ": %", ast.I.spelling, ast.T.pos);
                T = Environment.errorType;
                return T;
            }
        }

        I.visit(this, ast);

        // Unknown size of array
        if (E instanceof EmptyExpr) {
            if (T.isArray() && ((ArrayType) T).length == -1) {
                handler.reportError(errors[37] + ": %", ast.I.spelling, ast.T.pos);
                T = Environment.errorType;
            }
            return T;
        }

        Type returnType = (Type) E.visit(this, ast);
        if (!T.assignable(returnType) && !returnType.isError()) {
            String message = "expected " + T + ", received " + returnType;
            handler.reportError(errors[5] + ": %", message, E.pos);
            T = Environment.errorType;
            return T;
        }

        // May need to cast
        Expr e2AST = checkCast(ast.T, E, null);

        if (ast instanceof LocalVar V) {
            V.E = e2AST;
        } else if (ast instanceof GlobalVar V) {
            V.E = e2AST;
        }
        return T;
    }

    private Expr checkCast(Type expectedT, Expr expr, AST parent) {

        Type t = expr.type;
        if (expr instanceof ArrayIndexExpr) {
            t = ((ArrayType) expr.type).t;
        }
        if (expectedT.assignable(t) || t.isError() || expectedT.equals(t)) {
            return expr;
        }

        Type tFromAST = t;
        Type tToAST = expectedT;
        if (parent != null) {
            return new CastExpr(expr, tFromAST, tToAST, expr.pos, parent);
        } else {
            return new CastExpr(expr, tFromAST, tToAST, expr.pos);
        }
    }

    public Object visitCompoundStmt(CompoundStmt ast, Object o) {
        ast.SL.visit(this, o);
        List S = ast.SL;
        while (!(S instanceof EmptyStmtList)) {
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
        if (ast.parent instanceof Stmt S) {
            S.containsExit = true;
        } else if (ast.parent instanceof List S) {
            S.containsExit = true;
        }
        return null;
    }

    public Object visitContinueStmt(ContinueStmt ast, Object o) {
        if (this.loopDepth <= 0) {
            handler.reportError(errors[15], "", ast.pos);
        }
        ast.containsExit = true;
        if (ast.parent instanceof Stmt S) {
            S.containsExit = true;
        } else if (ast.parent instanceof List S) {
            S.containsExit = true;
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
        if (ast.parent instanceof Stmt S) {
            S.containsExit = true;
        } else if (ast.parent instanceof List S) {
            S.containsExit = true;
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

        if (decl instanceof LocalVar || decl instanceof GlobalVar) {
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

        Type existingType = decl.T;

        if (existingType.isArray()) {
            handler.reportError(errors[39] + ": %", ast.I.spelling, ast.E.pos);
            return null;
        }

        if (ast.isDeref) {
            if (!decl.T.isPointer()) {
                handler.reportError(errors[32], "", ast.pos);
                return null;
            }
            existingType = ((PointerType) decl.T).t;
        }

        Type t = (Type) ast.E.visit(this, o);
        if (!existingType.assignable(t)) {
            String message = "expected " + existingType + ", received " + t.toString();
            handler.reportError(errors[5] + ": %", message, ast.E.pos);
        }

        Expr e2AST;
        if (ast.opt == DeclOptions.ARRAY_ACC) {
            e2AST = checkCast(((ArrayType) existingType).t, ast.E, null);
        } else {
            e2AST = checkCast(existingType, ast.E, null);
        }
        ast.E = e2AST;
        return null;
    }

    public Object visitOperator(Operator ast, Object o) {
        return null;
    }

    public Object visitBinaryExpr(BinaryExpr ast, Object o) {
        ast.O.visit(this, ast);
        Type t1 = (Type) ast.E1.visit(this, ast);
        Type t2 = (Type) ast.E2.visit(this, ast);
        boolean v1Numeric = t1.isChar() || t1.isInt() || t1.isFloat() || t1.isEnum() || t1.isError();
        boolean v2Numeric = t2.isChar() || t2.isInt() || t2.isFloat() || t2.isEnum() || t2.isError();

        if (t1.isError() || t2.isError()) {
            ast.type = Environment.errorType;
            return ast.type;
        }

        switch (ast.O.spelling) {
            case "||", "&&" -> {
                if (!t1.isBoolean() || !t2.isBoolean()) {
                    handler.reportError(errors[7], "", t1.pos);
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
                if (!v1Numeric || !v2Numeric) {
                    handler.reportError(errors[7], "", t2.pos);
                    ast.type = Environment.errorType;
                    break;
                }
                ast.type = Environment.booleanType;
            }
            case "<", "<=", ">", ">=" -> {
                if (!v1Numeric || !v2Numeric) {
                    handler.reportError(errors[7], "", ast.pos);
                    ast.type = Environment.errorType;
                    break;
                }
               ast.type = Environment.booleanType;
            }
            case "+", "-", "/", "*", "%" -> {
                if (!v1Numeric || !v2Numeric) {
                    handler.reportError(errors[7], "", ast.pos);
                    ast.type = Environment.errorType;
                    break;
                }
                ast.type = t1;
            }
        }
        if (ast.type.isBoolean()) {
            if (t1.isChar() && t2.isChar()) {
                ast.O.spelling = "c" + ast.O.spelling;
                return ast.type;
            } else if (t1.isChar() && t2.isInt()) {
                ast.O.spelling = "i" + ast.O.spelling;
                ast.E1 = new CastExpr(ast.E1, t1, t2, ast.E1.pos, ast);
                return ast.type;
            } else if (t1.isInt() && t2.isChar()) {
                ast.O.spelling = "i" + ast.O.spelling;
                ast.E2= new CastExpr(ast.E2, t2, t1, ast.E2.pos, ast);
                return ast.type;
            }

            if (t1.isInt() && t2.isInt()) {
                ast.O.spelling = "i" + ast.O.spelling;
            } else if (t1.isInt() && t2.isFloat()) {
                ast.O.spelling = "f" + ast.O.spelling;
                Operator op = new Operator("i2f", ast.E1.pos);
                ast.E1 = new UnaryExpr(op, ast.E1, ast.E1.pos);
            } else if (t1.isFloat() && t2.isInt()) {
                ast.O.spelling = "f" + ast.O.spelling;
                Operator op = new Operator("i2f", ast.E2.pos);
                ast.E2 = new UnaryExpr(op, ast.E2, ast.E2.pos);
            } else if (t1.isFloat() && t2.isFloat()) {
                ast.O.spelling = "f" + ast.O.spelling;
            } else {
                ast.O.spelling = "b" + ast.O.spelling;
            }
            return ast.type;
        }

        if (t1.isChar() && t2.isChar()) {
            ast.O.spelling = "c" + ast.O.spelling;
            ast.type = Environment.charType;
            return ast.type;
        } else if (t1.isChar() && t2.isInt()) {
            ast.O.spelling = "i" + ast.O.spelling;
            ast.E1 = new CastExpr(ast.E1, t1, t2, ast.E1.pos, ast);
            ast.type = Environment.intType;
            return ast.type;
        } else if (t1.isInt() && t2.isChar()) {
            ast.O.spelling = "i" + ast.O.spelling;
            ast.E2= new CastExpr(ast.E2, t2, t1, ast.E2.pos, ast);
            ast.type = Environment.intType;
            return ast.type;
        }
        if (t1.isInt() && t2.isInt()) {
            ast.O.spelling = "i" + ast.O.spelling;
            ast.type = Environment.intType;
        } else if (t1.isInt() && t2.isFloat()) {
            ast.O.spelling = "f" + ast.O.spelling;
            ast.type = Environment.floatType;
            ast.E1= new CastExpr(ast.E1, t1, t2, ast.E1.pos, ast);
        } else if (t1.isFloat() && t2.isInt()) {
            ast.O.spelling = "f" + ast.O.spelling;
            ast.type = Environment.floatType;
            ast.E2= new CastExpr(ast.E2, t2, t1, ast.E2.pos, ast);
        } else if (t1.isFloat() && t2.isFloat()) {
            ast.O.spelling = "f" + ast.O.spelling;
            ast.type = Environment.floatType;
        }

        return ast.type;
    }

    public Object visitUnaryExpr(UnaryExpr ast, Object o) {
        ast.O.visit(this, ast);
        Type eT = (Type) ast.E.visit(this, ast);

        if (eT.isError()) {
            return ast.type;
        }

        switch (ast.O.spelling) {
            case "+", "-" -> {
                if (!eT.isInt() && !eT.isFloat()) {
                    handler.reportError(errors[8], "", ast.O.pos);
                    ast.type = Environment.errorType;
                    break;
                }
                ast.type = eT;
            }
            case "!" -> {
                if (!eT.isBoolean()) {
                    handler.reportError(errors[8], "", ast.O.pos);
                    ast.type = Environment.errorType;
                    break;
                }
                ast.type = eT;
            }
            case "*" -> { // de-reference operator
                if (!eT.isPointer()) {
                    String message = "dereference operator may only be applied to pointer types";
                    handler.reportError(errors[8] + ": %", message, ast.O.pos);
                    ast.type = Environment.errorType;
                    break;
                }
                if (!(ast.E instanceof VarExpr)) {
                    handler.reportError(errors[30], "", ast.O.pos);
                    break;
                }
                ast.type = ((PointerType) eT).t;
            }
            case "&" -> { // address of operator
                if (!(ast.E instanceof VarExpr)) {
                    handler.reportError(errors[30], "", ast.O.pos);
                    break;
                }
                VarExpr VE = (VarExpr) ast.E;
                SimpleVar SV = (SimpleVar) VE.V;
                Decl decl = idTable.retrieve(SV.I.spelling);
                if (!decl.isMut) {
                    handler.reportError(errors[31], "", ast.O.pos);
                    break;
                }
                ast.type = new PointerType(decl.pos, decl.T);
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

        ast.E = checkCast(expectedType, ast.E, ast);
        ast.EL.visit(this, ((ParaList) PL).PL);
        return null;
    }

    public Object visitParaList(ParaList ast, Object o) {
        ast.P.visit(this, null);
        ast.PL.visit(this, null);
        return null;
    }

    public Object visitEmptyParaList(EmptyParaList ast, Object o) {
        return null;
    }

    public Object visitParaDecl(ParaDecl ast, Object o) {
        Type T = ast.T;
        if (T.isMurky()) {
            unMurk(ast);
        }
        declareVariable(ast.I, ast);
        if (ast.T.isVoid()) {
            handler.reportError(errors[3] + ": %", ast.I.spelling, ast.I.pos);
        }
        if (ast.T.isArray()) {
            Type iT = ((ArrayType) ast.T).t;
            if (iT instanceof VoidType) {
                handler.reportError(errors[33] + ": %", ast.I.spelling, ast.T.pos);
            }
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

        boolean isFnCall = ((VarExpr) ast.parent).parent instanceof Args;
        if (decl.T.isArray() && !isFnCall) {
            handler.reportError(errors[34] + ": %", ast.I.spelling, ast.I.pos);
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
        return Environment.floatType;
    }

    public Object visitFloatType(FloatType ast, Object o) {
        return Environment.floatType;
    }

    public Object visitFloatExpr(FloatExpr ast, Object o) {
        ast.type = Environment.floatType;
        return ast.type;
    }

    public Object visitPointerType(PointerType ast, Object o) {
        Type t = (Type) ast.t.visit(this, o);
        return new PointerType(dummyPos, t);
    }

    public Object visitArrayType(ArrayType ast, Object o) {
        Type t = (Type) ast.t.visit(this, o);
        return new ArrayType(dummyPos, t, ast.length);
    }

    public Object visitArrayInitExpr(ArrayInitExpr ast, Object o) {
        Type expectedT = ((Decl) o).T;
        if (!expectedT.isArray()) {
            String message = "attempting to assign array to scalar";
            handler.reportError(errors[5] + ": %", message, ast.pos);
            return Environment.errorType;
        }
        ArrayType aT = (ArrayType) expectedT;
        Type iT = aT.t;
        int length = aT.length;

        if (ast.AL instanceof EmptyArgList) {
            if (length == -1) {
                aT.length = 0;
            }
            return aT;
        }

        Args args = (Args) ast.AL;
        int iterator = 1;

        boolean isError = false;
        boolean seenExcess = false;
        while (true) {
            Type t = (Type) args.E.visit(this, o);
            if (!iT.assignable(t)) {
                String message = "expected " + iT + ", received " + t + " at position " + (iterator - 1);
                handler.reportError(errors[36] + ": %", message, ast.pos);
                isError = true;
            }

            if (iterator > length && length != -1 && !seenExcess) {
                handler.reportError(errors[38] + ": %", ((Decl) o).I.spelling, ast.pos);
                isError = true;
                seenExcess = true;
            }

            if (args.EL instanceof EmptyArgList) {
                break;
            }

            iterator += 1;
            args = (Args) args.EL;
        }

        if (isError) {
            return Environment.errorType;
        }

        if (length == -1) {
            aT.length = iterator;
        }

        ast.type = aT;
        return aT;
    }

    public Object visitArrayIndexExpr(ArrayIndexExpr ast, Object o) {
        Decl binding = (Decl) ast.I.visit(this, o);
        if (binding == null) {
            handler.reportError(errors[4] + ": %", ast.I.spelling, ast.I.pos);
            return Environment.errorType;
        }
        Type T = (Type) ast.index.visit(this, o);
        if (!T.isInt()) {
            handler.reportError(errors[40] + ": %", ast.I.spelling, ast.I.pos);
            return Environment.errorType;
        }
        ast.type = binding.T;
        return ((ArrayType) binding.T).t;
    }

    public Object visitArrDeclStmt(DeclStmt ast, Object o) {
        Decl decl = idTable.retrieve(ast.I.spelling);
        if (decl == null) {
            handler.reportError(errors[4] + ": %", ast.I.spelling, ast.E.pos);
            return null;
        }
        if (decl instanceof LocalVar || decl instanceof GlobalVar) {
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

        Type t1 = (Type) ast.aeAST.get().visit(this, o);
        if (!t1.isInt()) {
            handler.reportError(errors[40] + ": %", ast.I.spelling, ast.E.pos);
            return null;
        }

        Type existingType = decl.T;
        Type innerType = ((ArrayType) existingType).t;
        if (ast.isDeref) {
            if (!innerType.isPointer()) {
                handler.reportError(errors[32], "", ast.pos);
                return null;
            }
            innerType = ((PointerType) innerType).t;
        }

        Type t = (Type) ast.E.visit(this, o);
        if (!innerType.assignable(t)) {
            String message = "expected " + existingType + ", received " + t.toString();
            handler.reportError(errors[5] + ": %", message, ast.E.pos);
        }

        if (t.isInt() && innerType.isFloat()) {
            Operator op = new Operator("i2f", ast.E.pos);
            ast.E = new UnaryExpr(op, ast.E, ast.E.pos);
        }
        return null;
    }

    public Object visitCharType(CharType ast, Object o) {
        return Environment.charType;
    }

    public Object visitCharLiteral(CharLiteral ast, Object o) {
        return Environment.charType;
    }

    public Object visitCharExpr(CharExpr ast, Object o) {
        int l = ast.CL.spelling.length();
        if (l != 1) {
            String m = "received '" + ast.CL.spelling + "'";
            handler.reportMinorError(errors[41] + ": %", m, ast.CL.pos);
            return Environment.errorType;
        }
        ast.type = Environment.charType;
        return ast.type;
    }

    public Object visitCastExpr(CastExpr ast, Object o) {
        // Never hit, they're constructed in this phase
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
            if (!(secondArg instanceof VarExpr || secondArg instanceof ArrayIndexExpr)) {
                handler.reportError(errors[18] + ": %", "Second arg for 'inInt' must be a declared int variable", ast.I.pos);
                return Environment.errorType;
            }
            Decl x = null;
            String spelling = null;
            if (secondArg instanceof VarExpr) {
                VarExpr VE = (VarExpr) secondArg;
                spelling = ((SimpleVar) VE.V).I.spelling;
                x = idTable.retrieve(spelling);
            } else {
                spelling = ((ArrayIndexExpr) secondArg).I.spelling;
                x =  idTable.retrieve(spelling);
            }
            if (x == null) {
                handler.reportError(errors[4] + ": %", spelling, ast.I.pos);
                return null;
            }
            x.isReassigned = true;
            if (!x.isMut) {
                handler.reportError(errors[23] + ": %", spelling, ast.I.pos);
                return null;
            }
        }

        // Check function exists
        String TL;
        boolean runArgsAgain = false;
        if (ast.TypeDef == null) {
            TL = genTypes(ast.AL, o);
            ast.setTypeDef(TL);
        } else {
            runArgsAgain = true;
            TL = ast.TypeDef;
        }

        Decl type = idTable.retrieve(ast.I.spelling + "." + TL);
        if (type != null) {
            ast.I.decl = type;
        }

        if (type == null) {
            handler.reportError(errors[4] + ": %", ast.I.spelling, ast.I.pos);
            return Environment.errorType;
        }

        // Check function is actually a function
        if (!(type instanceof Function function)) {
            handler.reportError(errors[10], "", ast.I.pos);
            return Environment.errorType;
        } else {
            Decl x = idTable.retrieve(ast.I.spelling + "." + TL);
            ((Function) x).setUsed();
        }
        if (runArgsAgain) {
            ast.AL.visit(this, function.PL);
        }
        ast.type = type.T;
        return type.T;
    }

    private String genTypes(List PL, Object o) {
        List head = PL;
        ArrayList<String> options = new ArrayList<>();
        while (!(head instanceof EmptyArgList)) {
            Expr D = ((Args) head).E;
            options.add(((Type) D.visit(this, o)).getMini());
            head = ((Args) head).EL;
        }
        return String.join("_", options);
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

    public Object visitStringExpr(StringExpr ast, Object o) {
        ast.type = new PointerType(ast.pos, new CharType(ast.pos));
        return ast.type;
    }

    public Object visitEnum(Enum ast, Object o) {
        return null;
    }

    public Object visitMurkyType(MurkyType ast, Object o) {
        return null;
    }

    public Object visitEnumType(EnumType ast, Object o) {
        return null;
    }

    public Object visitStringLiteral(StringLiteral ast, Object o) {
        return new PointerType(ast.pos, new CharType(ast.pos));
    }
}
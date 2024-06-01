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
        "*23: attempting to redeclare a constant" // 23 todo
    };

    private final SymbolTable idTable;
    private final ErrorHandler handler;

    private boolean hasMain = false;
    private boolean hasReturn = false;
    private boolean inMain = false;
    private int loopDepth = 0;
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
                stdFunction(F.T, F.I.spelling, F.PL);
            }
            if (L.DL instanceof EmptyDeclList) {
                break;
            }
            L = (DeclList) L.DL;
        }
        ast.visit(this, null);
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
        Environment.in = stdFunction(Environment.voidType, "in", new ParaList(
            new ParaDecl(Environment.strType, i, dummyPos, false),
            new EmptyParaList(dummyPos), dummyPos
        ));
        Environment.out = stdFunction(Environment.voidType, "out", new ParaList(
                new ParaDecl(Environment.voidType, i, dummyPos, false),
                new EmptyParaList(dummyPos), dummyPos
        ));
    }

    private Function stdFunction(Type resultType, String id, List pl) {
        Function binding = new Function(resultType, new Ident(id, dummyPos),
            pl, new EmptyStmt(dummyPos), dummyPos);
        idTable.insert(id, false, binding);
        return binding;
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
        }
        idTable.openScope();
        ast.PL.visit(this, null);
        ast.S.visit(this, ast);
        idTable.closeScope();
        if (!hasReturn && !ast.T.isVoid()) {
            handler.reportError(errors[22], "", ast.I.pos);
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
        Type returnType = (Type) E.visit(this, ast);
        if (!T.assignable(returnType) && !returnType.isError()) {
            String message = "expected " + T.toString() + ", received " + returnType.toString();
            handler.reportError(errors[5] + ": %", message, E.pos);
            T = Environment.errorType;
        }
        return T;
    }

    public Object visitCompoundStmt(CompoundStmt ast, Object o) {
        ast.SL.visit(this, o);
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
        if (!(ast.E1 instanceof EmptyExpr)) {
            ast.E1.visit(this, ast);
        }

        if (!(ast.E2 instanceof EmptyExpr)) {
            Type conditionType = (Type) ast.E2.visit(this, ast);
            if (!conditionType.isBoolean()) {
                handler.reportError(errors[12], "", ast.E2.pos);
            }
        }
        if (!(ast.E3 instanceof EmptyExpr)) {
            ast.E3.visit(this, ast);
        }
        this.loopDepth++;
        idTable.openScope();
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
        return null;
    }

    public Object visitContinueStmt(ContinueStmt ast, Object o) {
        if (this.loopDepth <= 0) {
            handler.reportError(errors[15], "", ast.pos);
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

        Type conditionType = (Type) ast.E.visit(this, ast);
        if (!this.currentFunctionType.assignable(conditionType)) {
            String message = "expected " + this.currentFunctionType.toString() +
                ", received " + conditionType.toString();
            handler.reportError(errors[6] + ": %", message, ast.E.pos);
        }

        return null;
    }

    public Object visitDeclStmt(DeclStmt ast, Object o) {

        Decl decl = idTable.retrieve(ast.I.spelling);
        if (decl == null) {
            handler.reportError(errors[4] + ": %", ast.I.spelling, ast.E.pos);
            return null;
        }

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
            return null;
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
                    ast.O.spelling = "i" + ast.O.spelling;
                }
            }
            case "==", "!=" -> {
                if (t1.isBoolean() && t2.isBoolean()) {
                    ast.type = Environment.booleanType;
                    ast.O.spelling = "i" + ast.O.spelling;
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
                ast.O.spelling = "i" +  ast.O.spelling;
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
                ast.O.spelling = "i" +  ast.O.spelling;
                ast.type = Environment.booleanType;
            }
            case "+", "-", "/", "*" -> {
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
                ast.O.spelling = "i" + ast.O.spelling;
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
                ast.O.spelling = "i" + ast.O.spelling;
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
            handler.reportError(errors[21], "", ast.SL.pos);
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
        if (((Function) PL.parent).I.spelling.equals("out")) {

        } else if (expectedType.isVoid() || !expectedType.assignable(realType)) {
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
        Decl decl = idTable.retrieve(ast.I.spelling);
        if (decl == null) {
            handler.reportError(errors[4] + ": %", ast.I.spelling, ast.I.pos);
            return Environment.errorType;
        }

        if (decl instanceof Function) {
            handler.reportError(errors[9], "", ast.I.pos);
            return Environment.errorType;
        }

        return decl.T;
    }


    public Object visitCallStmt(CallStmt ast, Object o) {
        ast.E.visit(this, o);
        return null;
    }

    public Object visitCallExpr(CallExpr ast, Object o) {
        if (inMain && ast.I.spelling.equals("main")) {
            handler.reportError(errors[20], "", ast.I.pos);
            return Environment.errorType;
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
        }
        ast.AL.visit(this, function.PL);
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

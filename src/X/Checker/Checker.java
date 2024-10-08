package X.Checker;

import X.Environment;
import X.ErrorHandler;
import X.Lexer.Position;
import X.Nodes.*;
import X.Nodes.Enum;
import X.Nodes.List;
import java.util.ArrayList;
import java.util.HashMap;

public class Checker implements Visitor {

    private final HashMap<String, Integer> stringConstantsMapping = new HashMap<>();
    private int strCount = 0;

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
        "*14: break must be in a loop construct",
        "*15: continue must be in a loop construct",
        "*16: main function may not have any parameters",
        "*17: main function may not call itself",
        "*18: statement(s) not reached",
        "*19: missing return statement",
        "*20: attempting to redeclare a constant",
        "*21: variable declared but never used",
        "*22: variable declared mutable but never reassigned",
        "*23: function declared but never used",
        "*24: inappropriate use of '$' operator",
        "*25: loop iterators must be integers",
        "*26: do-while conditional is not boolean",
        "*27: address-of operand only applicable to variables",
        "*28: can't get address of a constant variable",
        "*29: inappropriate dereference of variable",
        "*30: identifier declared void[]",
        "*31: attempt to use an array as a scalar",
        "*32: attempt to use a scalar/function as an array",
        "*33: wrong type for element in array initializer",
        "*34: unknown array size at compile time",
        "*35: excess elements in array initializer",
        "*36: attempted reassignment of array",
        "*37: array index is not an integer",
        "*38: char expr greater than one character",
        "*39: enum declared but never used",
        "*40: unknown type",
        "*41: unknown enum key",
        "*42: duplicate keys in enum",
        "*43: no function found with provided parameter types",
        "*44: type may not be emitted if expression not provided",
        "*45: duplicate struct members",
        "*46: struct declared but never used",
        "*47: multiple type definitions with same name",
        "*48: no struct members",
        "*49: no enum members",
        "*50: insufficient arguments to struct declaration",
        "*51: excess arguments to struct declaration",
        "*52: incompatible type for struct member",
        "*53: array in struct definition must have size defined",
        "*54: invalid left hand side for assignment expression"
    };

    private final SymbolTable idTable;
    private final ErrorHandler handler;

    private boolean hasMain = false;
    private boolean hasReturn = false;
    private boolean inMain = false;

    // Used for checking break/continue stmt
    private int loopDepth = 0;

    // Used for assigning
    private int loopAssignDepth = 0;

    // Base statement counter, used for constructing variable names
    private int baseStatementCounter = 0;

    private boolean validDollar = false;
    private Type currentFunctionType = null;

    private final Position dummyPos = new Position();

    public Checker(ErrorHandler handler) {
        this.handler = handler;
        this.idTable = new SymbolTable();
        establishEnv();
    }

    public void check(AST ast) {

        if (((Program) ast).PL instanceof EmptyDeclList) {
            handler.reportError(errors[0], "", ast.pos);
            return;
        }

        // Load  in all unique types
        DeclList L = (DeclList) ((Program) ast).PL;
        while (true) {
            if (L.D instanceof Enum E) {
                if (E.isEmpty()) {
                    handler.reportError(errors[49] + ": %", E.I.spelling, E.I.pos);
                } else {
                    // Duplicate enum keys
                    ArrayList<String> duplicates = E.findDuplicates();
                    if (!duplicates.isEmpty()) {
                        String message = "found keys '" + String.join(", ", duplicates)
                                + "' in enum '" + E.I.spelling + "'";
                        handler.reportError(errors[42] + ": %", message, E.I.pos);
                    }
                }
               // Duplicate type definitions
                Decl d = idTable.retrieve(E.I.spelling);
                if (d instanceof Enum) {
                    String message = "enum '" +E.I.spelling + "' clashes with previously declared enum";
                    handler.reportError(errors[47] + ": %", message, E.I.pos);
                } else if (d instanceof Struct) {
                    String message = "enum '" +E.I.spelling + "' clashes with previously declared struct";
                    handler.reportError(errors[47] + ": %", message, E.I.pos);
                }
                idTable.insert(E.I.spelling, E.isMut, E);
            } else if (L.D instanceof Struct S) {
                if (S.isEmpty()) {
                    handler.reportError(errors[48] + ": %", S.I.spelling, S.I.pos);
                } else {
                    ArrayList<String> duplicates = S.findDuplicates();
                    if (!duplicates.isEmpty()) {
                        String message = "found members '" + String.join(", ", duplicates)
                                + "' in struct '" + S.I.spelling + "'";
                        handler.reportError(errors[45] + ": %", message, S.I.pos);
                    }
                }

                // Duplicate type definitions
                Decl d = idTable.retrieve(S.I.spelling);
                if (d instanceof Enum) {
                    String message = "struct '" + S.I.spelling + "' clashes with previously declared enum";
                    handler.reportError(errors[47] + ": %", message, S.I.pos);
                } else if (d instanceof Struct) {
                    String message = "struct '" + S.I.spelling + "' clashes with previously declared struct";
                    handler.reportError(errors[47] + ": %", message, S.I.pos);
                }

                // header type is validated here
                // potential subtypes that are user-created types will be validated when struct's initialised
                idTable.insert(S.I.spelling, S.isMut, S);
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
                V.index = "";
                visitVarDecl(V, V.T, V.I, V.E);
            } else if (L.D instanceof Struct S) {
                List P = S.SL;
                // Recalculating struct members for abstract types
                if (!(P instanceof EmptyStructList)) {
                    while (true) {
                        StructElem SE = ((StructList) P).S;
                        Type T = SE.T;
                        if (T.isArray()) {
                            if (((ArrayType) T).length == -1) {
                                handler.reportError(errors[53] + ": %", SE.I.spelling, S.I.pos);
                            }
                        }
                        if (T.isMurky()) {
                            unMurk(SE);
                        } else if (T.isArray() && ((ArrayType) T).t.isMurky()) {
                            unMurkArr(SE);
                        } else if (T.isPointer() && ((PointerType) T).t.isMurky()) {
                            unMurkPointer(SE);
                        }
                        if (((StructList) P).SL instanceof EmptyStructList) {
                            break;
                        }
                        P = ((StructList) P).SL;
                    }
                }
            } else if (L.D instanceof Function F) {
                List P = F.PL;

                // Recalculating params for abstract types
                if (!(P instanceof EmptyParaList)) {
                    while (true) {
                        ParaDecl PE = ((ParaList) P).P;
                        Type T = PE.T;
                        if (T.isMurky()) {
                            unMurk(PE);
                        } else if (T.isArray() && ((ArrayType) T).t.isMurky()) {
                            unMurkArr(PE);
                        } else if (T.isPointer() && ((PointerType) T).t.isMurky()) {
                            unMurkPointer(PE);
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
                        handler.reportError(errors[40] + ": %", "'" + S + "'", F.T.pos);
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
                    handler.reportMinorError(errors[21] + ": %", message, V.pos);
                }
                if (V.isMut && !V.isReassigned) {
                    String message = "'" + V.I.spelling + "'";
                    handler.reportMinorError(errors[22] + ": %", message, V.pos);
                }
            } else if (L.D instanceof Function F) {
                if (!F.isUsed && !F.I.spelling.equals("main")) {
                    String message = "'" + F.I.spelling + "'";
                    handler.reportMinorError(errors[23] + ": %", message, F.pos);
                }
            } else if (L.D instanceof Enum E) {
                if (!E.isUsed) {
                    String message = "'" + E.I.spelling + "'";
                    handler.reportMinorError(errors[39] + ": %", message, E.pos);
                }
            } else if (L.D instanceof Struct S) {
                if (!S.isUsed) {
                    String message = "'" + S.I.spelling + "'";
                    handler.reportMinorError(errors[46] + ": %", message, S.pos);
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
        baseStatementCounter = 0;

        // Check if func already exists with that name
        this.currentFunctionType = ast.T;
        if (ast.I.spelling.equals("main")) {
            inMain = hasMain = true;
            if (!ast.T.isInt()) {
                String message = "set to " + ast.T.toString();
                handler.reportError(errors[1] + ": %", message, ast.I.pos);
            }
            if (!(ast.PL instanceof EmptyParaList)) {
               handler.reportError(errors[16], "", ast.I.pos);
            }
        } else if (ast.I.spelling.equals("$")) {
            handler.reportError(errors[24] + ": %", "can't be used as function name", ast.I.pos);
        }
        idTable.openScope();
        ast.PL.visit(this, null);
        ast.S.visit(this, ast);
        idTable.closeScope();
        if (!hasReturn && !ast.T.isVoid()) {
            if (!ast.I.spelling.equals("main")) {
                handler.reportError(errors[19], "", ast.I.pos);
            }
        }

        this.currentFunctionType = null;
        inMain = hasReturn = false;
        baseStatementCounter = 0;
        return ast.T;
    }

    public Object visitGlobalVar(GlobalVar ast, Object o) {
        return null;
    }

    private void unMurk(Decl ast) {
        String s = ((MurkyType) ast.T).V;
        Decl D = idTable.retrieve(s);
        if (D instanceof Enum E) {
            E.isUsed = true;
            ast.T = new EnumType(E, E.pos);
        } else if (D instanceof Struct S) {
            S.isUsed = true;
            ast.T = new StructType(S, S.pos);
        } else {
            handler.reportError(errors[40] + ": %", "'" + s + "'", ast.T.pos);
            ast.T = Environment.errorType;
        }
        ast.T.parent = ast;
    }

    private void unMurkArr(Decl ast) {
        ArrayType AT = (ArrayType) ast.T;
        String s = ((MurkyType) AT.t).V;
        Decl D = idTable.retrieve(s);
        if (D instanceof Enum E) {
            E.isUsed = true;
            ast.T = new ArrayType(AT.pos, new EnumType(E, E.pos), AT.length);
        } else if (D instanceof Struct S) {
            S.isUsed = true;
            ast.T = new ArrayType(AT.pos, new StructType(S, S.pos), AT.length);
        } else {
            handler.reportError(errors[40] + ": %", "'" + s + "'", ast.T.pos);
            ast.T = Environment.errorType;
        }
        ast.T.parent = ast;
    }

    private void unMurkPointer(Decl ast) {
        PointerType PT = (PointerType) ast.T;
        String s = ((MurkyType) PT.t).V;
        Decl D = idTable.retrieve(s);
        if (D instanceof Enum E) {
            E.isUsed = true;
            ast.T = new PointerType(PT.pos, new EnumType(E, E.pos));
        } else if (D instanceof Struct S) {
            S.isUsed = true;
            ast.T = new PointerType(PT.pos, new StructType(S, S.pos));
        } else {
            handler.reportError(errors[40] + ": %", "'" + s + "'", ast.T.pos);
            ast.T = Environment.errorType;
        }
        ast.T.parent = ast;
    }

    private Object visitVarDecl(Decl ast, Type T, Ident I, Expr E) {
        if (T.isMurky()) {
            unMurk(ast);
        } else if (T.isArray() && ((ArrayType) T).t.isMurky()) {
            unMurkArr(ast);
        } else if (T.isPointer() && ((PointerType) T).t.isMurky()) {
            unMurkPointer(ast);
        }
        T = ast.T;

        declareVariable(ast.I, ast);

        if (T.isVoid()) {
            handler.reportError(errors[3] + ": %", ast.I.spelling, ast.T.pos);
            T = Environment.errorType;
            return T;
        }

        if (T.isArray()) {
            Type iT = ((ArrayType) T).t;
            if (iT instanceof VoidType) {
                handler.reportError(errors[30] + ": %", ast.I.spelling, ast.T.pos);
                T = Environment.errorType;
                return T;
            }
        }

        I.visit(this, ast);

        // Unknown size of array
        if (E instanceof EmptyExpr) {

            if (ast.T.isUnknown()) {
                handler.reportError(errors[44] + ": %", ast.I.spelling, ast.I.pos);
                T = Environment.errorType;
            }

            if (T.isArray() && ((ArrayType) T).length == -1) {
                handler.reportError(errors[34] + ": %", ast.I.spelling, ast.T.pos);
                T = Environment.errorType;
            }
            return T;
        }

        Type returnType = (Type) E.visit(this, ast);

        if (ast.T.isUnknown()) {
           ast.T = returnType;
           ast.T.parent = ast;
           return ast.T;
        }

        if (returnType != null && !T.assignable(returnType) && !returnType.isError()) {
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

        // TODO: fix this, so lazy
        if (expectedT instanceof CharType && expr.type instanceof IntType) {
            CastExpr E = new CastExpr(expr, expr.type, expectedT, expr.pos);
            E.type = expectedT;
            return E;
        } else if (expectedT instanceof IntType && expr.type instanceof CharType) {
            CastExpr E = new CastExpr(expr, expr.type, expectedT, expr.pos);
            E.type = expectedT;
            return E;
        } else if (expectedT instanceof IntType && expr.type instanceof EnumType) {
            return expr;
        }
        // End TODO

        Type t = expr.type;
        if (expr instanceof ArrayIndexExpr) {
            t = ((ArrayType) expr.type).t;
        }
        if (expectedT.assignable(t) || t == null || t.isError() || expectedT.equals(t)) {
            return expr;
        }

        Type tFromAST = t;
        CastExpr E;
        if (parent != null) {
            E = new CastExpr(expr, tFromAST, expectedT, expr.pos, parent);
        } else {
            E = new CastExpr(expr, tFromAST, expectedT, expr.pos);
        }
        E.type = expectedT;
        return E;
    }

    private boolean isIsolatedCompoundStmt(Object o) {
        return !(o instanceof Function || o instanceof IfStmt || o instanceof ElseIfStmt || o instanceof WhileStmt
            || o instanceof ForStmt || o instanceof LoopStmt || o instanceof DoWhileStmt);
    }

    public Object visitCompoundStmt(CompoundStmt ast, Object o) {

        boolean isIsolatedCompoundStmt = isIsolatedCompoundStmt(ast.parent);
        if (isIsolatedCompoundStmt) {
            loopAssignDepth += 1;
        }

        ast.SL.visit(this, o);
        List S = ast.SL;
        while (!(S instanceof EmptyStmtList)) {
            StmtList SL = (StmtList) S;
            if (SL.S instanceof LocalVarStmt) {
                LocalVar V = ((LocalVarStmt) SL.S).V;
                if (!V.isUsed) {
                    String message = "'" + V.I.spelling + "'";
                    handler.reportMinorError(errors[21] + ": %", message, V.pos);
                }
                if (V.isMut && !V.isReassigned) {
                    String message = "'" + V.I.spelling + "'";
                    handler.reportMinorError(errors[22] + ": %", message, V.pos);
                }
            }
            S = SL.SL;
        }
        ast.containsExit = ast.SL.containsExit;

        if (isIsolatedCompoundStmt) {
            loopAssignDepth -= 1;
        }
        return null;
    }

    public Object visitLocalVar(LocalVar ast, Object o) {
        ast.index = String.format("%d_%d", loopAssignDepth, baseStatementCounter);
        ast.T = (Type) visitVarDecl(ast, ast.T, ast.I, ast.E);
        return ast.T;
    }

    public Object visitIfStmt(IfStmt ast, Object o) {
        Type condT = (Type) ast.E.visit(this, ast);
        if (!condT.isBoolean()) {
            handler.reportError(errors[11], "", ast.E.pos);
        }

        idTable.openScope();
        loopAssignDepth += 1;
        ast.S1.visit(this, ast);
        loopAssignDepth -= 1;
        idTable.closeScope();

        idTable.openScope();
        ast.S2.visit(this, ast);
        idTable.closeScope();

        idTable.openScope();
        loopAssignDepth += 1;
        ast.S3.visit(this, ast);
        loopAssignDepth -= 1;
        idTable.closeScope();

        return null;
    }

    public Object visitElseIfStmt(ElseIfStmt ast, Object o) {
        Type condT = (Type) ast.E.visit(this, ast);
        if (!condT.isBoolean()) {
            handler.reportError(errors[11], "", ast.E.pos);
        }

        idTable.openScope();
        loopAssignDepth += 1;
        ast.S1.visit(this, ast);
        loopAssignDepth -= 1;
        idTable.closeScope();

        idTable.openScope();
        ast.S2.visit(this, ast);
        idTable.closeScope();

        return null;
    }

    public Object visitForStmt(ForStmt ast, Object o) {
        idTable.openScope();
        loopAssignDepth += 1;
        ast.S1.visit(this, ast);
        if (!(ast.E2 instanceof EmptyExpr)) {
            Type conditionType = (Type) ast.E2.visit(this, ast);
            if (!conditionType.isBoolean()) {
                handler.reportError(errors[12], "", ast.E2.pos);
            }
        }
        ast.S3.visit(this, ast);
        loopDepth++;
        ast.S.visit(this, ast);
        idTable.closeScope();
        loopDepth--;
        loopAssignDepth -= 1;
        return null;
    }

    public Object visitWhileStmt(WhileStmt ast, Object o) {
        Type conditionType = (Type) ast.E.visit(this, ast);
        if (!conditionType.isBoolean()) {
            handler.reportError(errors[13], "", ast.E.pos);
        }
        loopDepth++;
        loopAssignDepth += 1;
        idTable.openScope();
        ast.S.visit(this, ast);
        idTable.closeScope();
        loopAssignDepth -= 1;
        loopDepth--;
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
        boolean seenIncompatible = false;

        // Returning nothing but there's something to return
        if (ast.E instanceof EmptyExpr && !(this.currentFunctionType.isVoid())) {
            seenIncompatible = true;
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
        if (!this.currentFunctionType.assignable(conditionType) && !seenIncompatible) {
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
                if (eT instanceof IntType) {
                    ast.O.spelling = "i" + ast.O.spelling;
                } else if (eT instanceof CharType) {
                    ast.O.spelling = "c" + ast.O.spelling;
                } else if (eT instanceof FloatType) {
                    ast.O.spelling = "f" + ast.O.spelling;
                }

                ast.type = eT;
            }
            case "!" -> {
                if (!eT.isBoolean()) {
                    handler.reportError(errors[8], "", ast.O.pos);
                    ast.type = Environment.errorType;
                    break;
                }
                ast.O.spelling = "b" + ast.O.spelling;
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
                    handler.reportError(errors[27], "", ast.O.pos);
                    break;
                }
                ast.type = ((PointerType) eT).t;
            }
            case "&" -> { // address of operator
                if (!(ast.E instanceof VarExpr)) {
                    handler.reportError(errors[27], "", ast.O.pos);
                    break;
                }
                VarExpr VE = (VarExpr) ast.E;
                SimpleVar SV = (SimpleVar) VE.V;
                Decl decl = idTable.retrieve(SV.I.spelling);
                if (!decl.isMut) {
                    handler.reportError(errors[28], "", ast.O.pos);
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
        baseStatementCounter += 1;
        if (ast.S instanceof CompoundStmt) {
            idTable.openScope();
            ast.S.visit(this, o);
            idTable.closeScope();
        } else {
            ast.S.visit(this, o);
        }
        // Unreached statements
        if (ast.S instanceof ReturnStmt && ast.SL instanceof StmtList) {
            handler.reportMinorError(errors[18], "", ast.SL.pos);
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
        List PL = (List) o;
        Type realType = (Type) ast.E.visit(this, null);

        Type expectedType = ((ParaList) PL).P.T;

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
        } else if (T.isArray() && ((ArrayType) T).t.isMurky()) {
            unMurkArr(ast);
        } else if (T.isPointer() && ((PointerType) T).t.isMurky()) {
            unMurkPointer(ast);
        }
        declareVariable(ast.I, ast);
        if (ast.T.isVoid()) {
            handler.reportError(errors[3] + ": %", ast.I.spelling, ast.I.pos);
        }
        if (ast.T.isArray()) {
            Type iT = ((ArrayType) ast.T).t;
            if (iT instanceof VoidType) {
                handler.reportError(errors[30] + ": %", ast.I.spelling, ast.T.pos);
            }
        }
        return null;
    }

    private void declareVariable(Ident ident, Decl decl) {
        if (ident.spelling.equals("$")) {
            handler.reportError(errors[24] +  ": %", "Can't use '$' operator as variable", ident.pos);
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
        // Assumes that when a function parameter is declared mutable
        // It actually is mutated
        Var V = ast.V;
        Decl decl = idTable.retrieve(((SimpleVar) ast.V).I.spelling);
        if (decl != null) {
            decl.isReassigned = true;
        }
        ast.type = (Type) ast.V.visit(this, o);
        return ast.type;
    }

    public Object visitSimpleVar(SimpleVar ast, Object o) {

        if (ast.I.spelling.equals("$") && !validDollar) {
            handler.reportError(errors[24] + ": %", ast.I.spelling, ast.I.pos);
            return Environment.errorType;
        } else if (ast.I.spelling.equals("$")) {
            return Environment.intType;
        }

        Decl decl = idTable.retrieve(ast.I.spelling);
        if (decl == null) {
            handler.reportError(errors[4] + ": %", ast.I.spelling, ast.I.pos);
            return Environment.errorType;
        }

        ast.I.decl = decl;
        boolean isFnCall = ast.parent.parent instanceof Args;
        boolean isStructDecl = ast.parent.parent instanceof StructArgs;
        if (decl.T.isArray() && !isFnCall && !isStructDecl) {
            handler.reportError(errors[31] + ": %", ast.I.spelling, ast.I.pos);
            return Environment.errorType;
        }

        if (decl instanceof Function) {
            handler.reportError(errors[9], "", ast.I.pos);
            return Environment.errorType;
        } else if (decl instanceof GlobalVar || decl instanceof LocalVar) {
            ((Decl)ast.I.decl).isUsed = true;
        }

        return decl.T;
    }

    public Object visitLoopStmt(LoopStmt ast, Object o) {
        Type T1, T2;

        idTable.openScope();
        ast.varName.ifPresent(localVar -> declareVariable(localVar.I, localVar));
        ast.varName.ifPresent(localVar -> localVar.index = String.format("%d_%d", loopAssignDepth, baseStatementCounter));
        validDollar = ast.varName.isEmpty();
        if (ast.I1.isPresent()) {
            T1 = (Type) ast.I1.get().visit(this, o);
            if (!T1.isInt()) {
                handler.reportError(errors[25], "", ast.pos);
            }
        }
        if (ast.I2.isPresent()) {
            T2 = (Type) ast.I2.get().visit(this, o);
            if (!T2.isInt()) {
                handler.reportError(errors[25], "", ast.pos);
            }
        }

        loopDepth++;
        loopAssignDepth += 1;
        ast.S.visit(this, o);
        loopAssignDepth -= 1;
        loopDepth--;
        idTable.closeScope();
        validDollar = false;
        return null;
    }

    public Object visitDoWhileStmt(DoWhileStmt ast, Object o) {
        this.loopDepth++;
        idTable.openScope();
        loopAssignDepth += 1;
        ast.S.visit(this, ast);
        loopAssignDepth -= 1;
        idTable.closeScope();
        this.loopDepth--;
        Type conditionType = (Type) ast.E.visit(this, ast);
        if (!conditionType.isBoolean()) {
            handler.reportError(errors[26], "", ast.E.pos);
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

    private Object handleUnknownArr(ArrayInitExpr ast, Object o) {

        if (ast.AL instanceof EmptyArgList) {
            handler.reportError(errors[44] + ": %", "array type cannot be deduced from 0 elements", ast.pos);
            return Environment.errorType;
        }
        int length = 0;
        List args = ast.AL;
        Type T = (Type) ((Args) args).E.visit(this, o);

        length++;
        args = ((Args) args).EL;
        while (!(args instanceof EmptyArgList)) {
            Type t = (Type) ((Args) args).E.visit(this, o);
            if (!t.assignable(T)) {
                String message = "expected " + T + ", received " + t + " at position " + length;
                handler.reportError(errors[33] + ": %", message, ast.pos);
            }
            ((Args) args).E = checkCast(T, ((Args) args).E, ast);
            ((Args) args).E.parent = ast.AL;
            length++;
            args = ((Args) args).EL;
        }
        return new ArrayType(ast.pos, T, length);
    }

    public Object visitArrayInitExpr(ArrayInitExpr ast, Object o) {
        Type expectedT = ((Decl) o).T;

        if (expectedT.isUnknown()) {
            ast.type = (Type) handleUnknownArr(ast, o);
            return ast.type;
        }

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
            if (!t.assignable(iT)) {
                String message = "expected " + iT + ", received " + t + " at position " + (iterator - 1);
                handler.reportError(errors[33] + ": %", message, ast.pos);
                isError = true;
            }
            args.E = checkCast(iT, args.E, ast);
            args.E.parent = ast.AL;

            if (iterator > length && length != -1 && !seenExcess) {
                handler.reportError(errors[35] + ": %", ((Decl) o).I.spelling, ast.pos);
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
        if (binding instanceof Function || !binding.T.isArray()) {
            handler.reportError(errors[32] + ": %", ast.I.spelling, ast.I.pos);
            return Environment.errorType;
        }

        if (!ast.isLHSOfAssignment) {
            binding.isUsed = true;
        } else {
            binding.isReassigned = true;
        }
        Type T = (Type) ast.index.visit(this, o);
        if (!T.isInt()) {
            handler.reportError(errors[37] + ": %", ast.I.spelling, ast.I.pos);
            return Environment.errorType;
        }
        ast.type = binding.T;
        return ((ArrayType) binding.T).t;
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
            handler.reportMinorError(errors[38] + ": %", m, ast.CL.pos);
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
            handler.reportError(errors[17], "", ast.I.pos);
            return Environment.errorType;
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

        Decl type = idTable.retrieve(ast.I.spelling);
        if (type == null) {
            handler.reportError(errors[4] + ": %", ast.I.spelling, ast.I.pos);
            ast.type = Environment.errorType;
            return ast.type;
        }

        // Check function is actually a function
        if (!(type instanceof Function function)) {
            handler.reportError(errors[10], "", ast.I.pos);
            return Environment.errorType;
        } else {
            // Need to get the right one
            type = idTable.retrieveFunc(ast.I.spelling + "." + ast.TypeDef);
            if (type == null) {
                handler.reportError(errors[43] + ": %", ast.I.spelling, ast.I.pos);
                ast.type = Environment.errorType;
                return ast.type;
            }
            ast.I.decl = type;
            ((Function) type).setUsed();
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
            System.out.println("UNREACHABLE");
        }
        return null;
    }

    public Object visitLocalVarStmt(LocalVarStmt ast, Object o) {
        ast.V.visit(this, o);
        return null;
    }

    public Object visitStringExpr(StringExpr ast, Object o) {
        String v = ast.SL.spelling;
        if (stringConstantsMapping.containsKey(v)) {
            ast.index = stringConstantsMapping.get(v);
            ast.needToEmit = false;
        } else {
            ast.needToEmit = true;
            stringConstantsMapping.put(v, strCount);
            ast.index = strCount++;
        }
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

    public Object visitEnumExpr(EnumExpr ast, Object o) {

        Decl d = idTable.retrieve(ast.Type.spelling);
        if (!(d instanceof Enum E)) {
            handler.reportError(errors[40] + ": %", ast.Type.spelling, ast.pos);
            return Environment.errorType;
        }
        E.isUsed = true;
        if (!E.containsKey(ast.Entry.spelling)) {
            String message = "'" + ast.Entry.spelling + "' in enum '" + ast.Type.spelling + "'";
            handler.reportError(errors[41] + ": %", message, ast.pos);
            return Environment.errorType;
        }
        EnumType ET = new EnumType(E, E.pos);
        ast.type = ET;
        return ET;
    }

    public Object visitUnknownType(UnknownType ast, Object o) {
        return null;
    }

    public Object visitStructElem(StructElem ast, Object o) {
        return null;
    }

    public Object visitStructList(StructList ast, Object o) {
        return null;
    }

    public Object visitStruct(Struct ast, Object o) {
        return null;
    }

    public Object visitEmptyStructList(EmptyStructList ast, Object o) {
        return null;
    }

    public Object visitStructType(StructType ast, Object o) {
        return null;
    }

    public Object visitEmptyStructArgs(EmptyStructArgs ast, Object o) {
        return null;
    }

    public Object visitStructArgs(StructArgs ast, Object o) {
        StructList L = (StructList) o;
        Type expectedType = L.S.T;
        Type realType = (Type) ast.E.visit(this, null);
        ast.E.type = realType;
        if (!realType.assignable(expectedType)) {
            String message = "member '" + L.S.I.spelling + "' should be of type " +
                expectedType + ", but received " + realType;
            handler.reportError(errors[52] + ": %", message, ast.pos);
        }
        ast.SL.visit(this, L.SL);
        return null;
    }

    public Object visitStructExpr(StructExpr ast, Object o) {
        String name = ast.I.spelling;
        Decl D = idTable.retrieve(name);
        if (!(D instanceof Struct DS)) {
            handler.reportError(errors[40] + ": %", "'" + name + "'", ast.I.pos);
            return Environment.errorType;
        }
        DS.isUsed = true;
        int numExpectedArgs = DS.getLength();
        int realNumArgs = ast.getLength();
        String message = "'" + name + "' expects " + numExpectedArgs + " argument/s but received " + realNumArgs;
        if (realNumArgs < numExpectedArgs) {
            handler.reportError(errors[50] + ": %" ,message, ast.SA.pos);
        } else if (realNumArgs > numExpectedArgs) {
            handler.reportError(errors[51] + ": %" ,message, ast.SA.pos);
        }
        ast.SA.visit(this, DS.SL);
        Type T = new StructType(DS, DS.pos);
        ast.type = T;
        return T;
    }

    private boolean validLHS(Object o) {
        return o instanceof ArrayIndexExpr || o instanceof VarExpr
            || o instanceof DerefExpr;
    }

    public Object visitAssignmentExpr(AssignmentExpr ast, Object o) {
        if (ast.LHS instanceof VarExpr VE) {
            Decl D = idTable.retrieve(((SimpleVar) VE.V).I.spelling);
            if (D.T.isArray()) {
                handler.reportError(errors[36],  "", ast.LHS.pos);
                return Environment.errorType;
            }
        }

        if (!validLHS(ast.LHS)) {
            String message = "must be a variable or an array index";
            handler.reportError(errors[54] + ": %", message, ast.LHS.pos);
            return Environment.errorType;
        }

        Type realType = (Type) ast.RHS.visit(this, o);
        Type expectedType = (Type) ast.LHS.visit(this, o);

        if (!realType.assignable(expectedType)) {
            String message = "expected " + expectedType + ", received " + realType;
            handler.reportError(errors[5] + ": %", message, ast.LHS.pos);
            return Environment.errorType;
        }
        if (ast.LHS instanceof VarExpr V && !expectedType.isError()) {
            Decl D = (Decl) ((SimpleVar) V.V).I.decl;
            if (!D.isMut) {
                String message = "'" + D.I.spelling + "'";
                handler.reportError(errors[20] + ": %", message, ast.LHS.pos);
                return Environment.errorType;
            }
        } else if (ast.LHS instanceof ArrayIndexExpr A && !expectedType.isError()) {
            Decl D = (Decl) A.I.decl;
            if (!D.isMut) {
                String message = "'" + D.I.spelling + "'";
                handler.reportError(errors[20] + ": %", message, ast.LHS.pos);
                return Environment.errorType;
            }
        }

        if (ast.O.spelling.equals("/=") || ast.O.spelling.equals("*=")
            || ast.O.spelling.equals("-=")|| ast.O.spelling.equals("+=")) {
            String O = String.valueOf(ast.O.spelling.charAt(0));
            ast.RHS = new BinaryExpr(ast.LHS, ast.RHS, new Operator(O, ast.RHS.pos), ast.RHS.pos);
            ast.RHS.visit(this, o);
        }


        ast.RHS = checkCast(expectedType, ast.RHS, ast);
        ast.type = expectedType;
        return ast.type;
    }

    public Object visitDerefExpr(DerefExpr ast, Object o) {
        Type T = (Type) ast.E.visit(this, o);
        if (!T.isPointer()) {
            handler.reportError(errors[29], "", ast.pos);
            return Environment.errorType;
        }
        ast.type = ((PointerType) T).t;
        return ((PointerType) T).t;
    }

    public Object visitExprStmt(ExprStmt ast, Object o) {
        if (!(ast.E instanceof AssignmentExpr || ast.E instanceof CallExpr)) {
            System.out.println(ast.E);
            System.out.println("Should never be reached");
        }
        ast.E.visit(this, o);
        return null;
    }

    public Object visitStringLiteral(StringLiteral ast, Object o) {
        return new PointerType(ast.pos, new CharType(ast.pos));
    }
}
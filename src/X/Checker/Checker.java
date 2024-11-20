package X.Checker;

import X.AllModules;
import X.Environment;
import X.ErrorHandler;
import X.Lexer.Lex;
import X.Lexer.MyFile;
import X.Lexer.Position;
import X.Lexer.Token;
import X.Nodes.*;
import X.Nodes.Enum;
import X.Nodes.Module;
import X.Parser.Parser;
import X.Parser.SyntaxError;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public class Checker implements Visitor {

    private final HashMap<String, Integer> stringConstantsMapping = new HashMap<>();
    private int strCount = 0;
    private boolean isStructLHS = false;

    private Type currentNumericalType = null;

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
        "*32: attempt to use a scalar/function/struct member as an array",
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
        "*54: invalid left hand side for assignment expression",
        "*55: no nested structure permissible in enum",
        "*56: dot access impermissible on non variables",
        "*57: dot access only permissible on struct variables",
        "*58: non-existent struct member",
        "*59: subtype is not a struct type",
        "*60: struct field is not mutable",
        "*61: imported file does not exist",
        "*62: imported file already imported",
        "*63: alias does not exist for module access",
        "*64: can't access variable/function/type that is not exported",
        "*65: no such function in module",
        "*66: no such variable in module",
        "*67: no such type in module",
        "*68: no such lib C function"
    };

    private final SymbolTable idTable;
    private final ErrorHandler handler;

    private boolean hasMain = false;
    private boolean hasReturn = false;
    private boolean inMain = false;
    private boolean declaringLocalVar = false;

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
    }

    private AllModules modules = AllModules.getInstance();
    private Module mainModule;

    private String currentFileName;

    private String[] numPriority = {"I8", "I32", "I64", "F32", "F64"}; // Later ones take priority

    public ArrayList<Module> check(AST ast, String filename, boolean isMain) {

        currentFileName = filename;
        
        mainModule = new Module(filename, isMain);
        mainModule.thisHandler = handler;
        modules.addModule(mainModule);

        establishEnv();

        if (((Program) ast).PL instanceof EmptyDeclList) {
            if (isMain) {
                handler.reportError(errors[0], "", ast.pos);
            }
            return null;
        }

        DeclList L = (DeclList) ((Program) ast).PL;

        AST v = loadModules(L);

        // Load  in all unique types
        loadUniqueTypes(L);
       
        // Load in function names and global vars
        loadFunctionsAndGlobalVars(L);
       
        // Actually visiting everything
        v.visit(this, null);

        // Checking use of variables/reassignment
        if (!handler.isQuiet && isMain) {
            checkUnusedEntities();
        }

        if (!hasMain && isMain) {
            handler.reportError(errors[0], "", ast.pos);
        }

        return modules.getModules();
    }

    private AST loadModules(DeclList L) {
        while (true) {
            if (L.D.isImportStmt()) {
                L.D.visit(this, null);
            } else {
                return L;
            }

            if (L.DL.isEmptyDeclList()) {
                break;
            }
            L = (DeclList) L.DL;
        }
        return null;
    }

    private void loadUniqueTypes(DeclList L) {
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
                if (mainModule.enumExists(E.I.spelling)) {
                    String message = "enum '" +E.I.spelling + "' clashes with previously declared enum";
                    handler.reportError(errors[47] + ": %", message, E.I.pos);
                } else if (mainModule.structExists(E.I.spelling)) {
                    String message = "enum '" +E.I.spelling + "' clashes with previously declared struct";
                    handler.reportError(errors[47] + ": %", message, E.I.pos);
                }

                mainModule.addEnum(E);

            } else if (L.D instanceof Struct S) {

                S.fileName = currentFileName;
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
                if (mainModule.enumExists(S.I.spelling)) {
                    String message = "struct '" + S.I.spelling + "' clashes with previously declared enum";
                    handler.reportError(errors[47] + ": %", message, S.I.pos);
                } else if (mainModule.structExists(S.I.spelling)) {
                    String message = "struct '" + S.I.spelling + "' clashes with previously declared struct";
                    handler.reportError(errors[47] + ": %", message, S.I.pos);
                }

                // header type is validated here
                // potential subtypes that are user-created types will be validated when struct's initialised
                mainModule.addStruct(S);
            }

            if (L.DL instanceof EmptyDeclList) {
                break;
            }
            L = (DeclList) L.DL;
        }
    }

    private void loadFunctionsAndGlobalVars(DeclList L) {
        while (true) {
            if (L.D instanceof GlobalVar V) {
                V.index = "";
                visitVarDecl(V, V.T, V.I, V.E);
            } else if (L.D instanceof Struct S) {
                List P = S.SL;
                // Recalculating struct members for abstract types
                if (!P.isEmptyStructList()) {
                    while (true) {
                        StructElem SE = ((StructList) P).S;
                        Type T = SE.T;
                        if (T.isArray()) {
                            if (((ArrayType) T).length == -1) {
                                handler.reportError(errors[53] + ": %", SE.I.spelling, S.I.pos);
                            }
                        }
                        checkMurking(SE);
                        if (((StructList) P).SL.isEmptyStructList()) {
                            break;
                        }
                        P = ((StructList) P).SL;
                    }
                }
            } else if (L.D instanceof Function F) {
                List P = F.PL;

                // Recalculating params for abstract types
                if (!P.isEmptyParaList()) {
                    while (true) {
                        ParaDecl PE = ((ParaList) P).P;
                        checkMurking(PE);
                        if (((ParaList) P).PL.isEmptyParaList()) {
                            break;
                        }
                        P = ((ParaList) P).PL;
                    }
                }

                if (F.T.isMurky()) {
                    F.T = unMurk((MurkyType) F.T);
                }

                if (mainModule.functionExists(F.I.spelling)) {
                    Function e = mainModule.getFunction(F.I.spelling + "." + F.TypeDef);
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
            if (L.DL.isEmptyDeclList()) {
                break;
            }
            L = (DeclList) L.DL;
        }
    }

    private void checkUnusedEntities() {
        for (Module M: AllModules.getInstance().getModules()) {
            for (Enum e: M.getEnums().values()) {
                if (!e.isUsed) {
                    M.thisHandler.reportMinorError(errors[39] + ": %", e.I.spelling, e.I.pos);
                }
            }

            for (GlobalVar v: M.getVars().values()) {
                if (!v.isUsed) {
                    M.thisHandler.reportMinorError(errors[21] + ": %", v.I.spelling, v.I.pos);
                }
                if (v.isMut && !v.isReassigned) {
                    M.thisHandler.reportMinorError(errors[22] + ": %", v.I.spelling, v.I.pos);
                }
            }

            for (Function f: M.getFunctions().values()) {
                if (!f.isUsed && !f.I.spelling.equals("main")) {
                    M.thisHandler.reportMinorError(errors[23] + ": %", f.I.spelling, f.I.pos);
                }
            }

            for (Struct s: M.getStructs().values()) {
                if (!s.isUsed) {
                    M.thisHandler.reportMinorError(errors[46] + ": %", s.I.spelling, s.I.pos);
                }
            }
        }
    }

    private void establishEnv() {
        Ident i = new Ident("x", dummyPos);
        AnyType anyType = new AnyType(dummyPos);
        PointerType voidPointerType = new PointerType(dummyPos, anyType);
        Environment.booleanType = new BooleanType(dummyPos);
        Environment.i8Type= new I8Type(dummyPos);
        Environment.i64Type = new I64Type(dummyPos);
        Environment.i32Type = new I32Type(dummyPos);
        Environment.f32Type = new F32Type(dummyPos);
        Environment.f64Type = new F64Type(dummyPos);
        Environment.voidType = new VoidType(dummyPos);
        Environment.errorType = new ErrorType(dummyPos);
        Environment.charPointerType = new PointerType(dummyPos, Environment.i8Type);
        Environment.outI64 = stdFunction(Environment.voidType, "outI64", new ParaList(
                new ParaDecl(Environment.i64Type, i, dummyPos, false),
                new EmptyParaList(dummyPos), dummyPos
        ));
        Environment.outChar = stdFunction(Environment.voidType, "outChar", new ParaList(
                new ParaDecl(Environment.i8Type, i, dummyPos, false),
                new EmptyParaList(dummyPos), dummyPos
        ));
        Environment.outStr = stdFunction(Environment.voidType, "outStr", new ParaList(
                new ParaDecl(Environment.charPointerType, i, dummyPos, false),
                new EmptyParaList(dummyPos), dummyPos
        ));
        Environment.outF32 = stdFunction(Environment.voidType, "outF32", new ParaList(
                new ParaDecl(Environment.f32Type, i, dummyPos, false),
                new EmptyParaList(dummyPos), dummyPos
        ));
        Environment.outF64 = stdFunction(Environment.voidType, "outF64", new ParaList(
                new ParaDecl(Environment.f64Type, i, dummyPos, false),
                new EmptyParaList(dummyPos), dummyPos
        ));
        Environment.malloc = stdFunction(voidPointerType, "malloc", new ParaList(
                new ParaDecl(Environment.i64Type, i, dummyPos, false),
                new EmptyParaList(dummyPos), dummyPos
        ));
        Environment.free = stdFunction(Environment.voidType, "free", new ParaList(
                new ParaDecl(voidPointerType, i, dummyPos, false),
                new EmptyParaList(dummyPos), dummyPos
        ));


        // Lib C ones
        Environment.sin = stdFunctionLibC(Environment.f64Type, "sin", new ParaList(
                new ParaDecl(Environment.f64Type, i, dummyPos, false),
                new EmptyParaList(dummyPos), dummyPos
        ));
        Environment.cos = stdFunctionLibC(Environment.f64Type, "cos", new ParaList(
                new ParaDecl(Environment.f64Type, i, dummyPos, false),
                new EmptyParaList(dummyPos), dummyPos
        ));
        Environment.pow = stdFunctionLibC(Environment.f64Type, "pow", new ParaList(
                new ParaDecl(Environment.f64Type, i, dummyPos, false),
                new ParaList(
                    new ParaDecl(Environment.f64Type, i, dummyPos, false),
                    new EmptyParaList(dummyPos), dummyPos
                ), dummyPos
        ));
   }

    private Function stdFunctionLibC(Type resultType, String id, List pl) {
        Function binding = new Function(resultType, new Ident(id, dummyPos),
            pl, new EmptyStmt(dummyPos), dummyPos);
        binding.setTypeDef();
        binding.isUsed = true;
        if (!modules.libCFunctionExists(id)) {
            modules.addLibCFunction(binding);
        }
        return binding;
    }

    private Function stdFunction(Type resultType, String id, List pl) {
        Function binding = new Function(resultType, new Ident(id, dummyPos),
            pl, new EmptyStmt(dummyPos), dummyPos);
        binding.setTypeDef();
        binding.isUsed = true;
        mainModule.addFunction(binding);
        return binding;
    }

    private void stdFunction(Function funct) {
        mainModule.addFunction(funct);
    }

    public Object visitProgram(Program ast, Object o) {
        ast.PL.visit(this, null);
        return null;
    }

    public Object visitIdent(Ident ast, Object o) {
        assert(!ast.isModuleAccess);
        Decl binding = idTable.retrieve(ast.spelling);
        if (binding != null) {
            ast.decl = binding;
        } else {
            if (mainModule.varExists(ast.spelling)) {
                binding = mainModule.getVar(ast.spelling);
                ast.decl = binding;
            }
        }
        return binding;
    }

    public Object visitFunction(Function ast, Object o) {
        baseStatementCounter = 0;

        // Check if func already exists with that name
        this.currentFunctionType = ast.T;
        if (ast.I.spelling.equals("main")) {
            inMain = hasMain = true;
            if (!ast.T.isI64()) {
                String message = "set to " + ast.T.toString();
                handler.reportError(errors[1] + ": %", message, ast.I.pos);
            }
            if (!ast.PL.isEmptyParaList()) {
               handler.reportError(errors[16], "", ast.I.pos);
            }
        } else if (ast.I.spelling.equals("$")) {
            handler.reportError(errors[24] + ": %", "can't be used as function name", ast.I.pos);
        }

        idTable.openScope();
        ast.PL.visit(this, null);
        ast.S.visit(this, ast);
        idTable.closeScope();
        if (!hasReturn && !ast.T.isVoid() && !ast.I.spelling.equals("main")) {
            handler.reportError(errors[19], "", ast.I.pos);
        }

        this.currentFunctionType = null;
        inMain = hasReturn = false;
        baseStatementCounter = 0;
        return ast.T;
    }

    public Object visitGlobalVar(GlobalVar ast, Object o) {
        return null;
    }

    private Type unMurk(MurkyType type) {
        String s = type.V.spelling;
        Module M = mainModule;

        Type T = null;
        if (type.V.isModuleAccess) {
            String moduleRef = type.V.module.get();
            if (!mainModule.aliasExists(moduleRef)) {
                handler.reportError(errors[63] + ": %", "'" + moduleRef + "'", type.pos);
                T = Environment.errorType;
            }
            M = mainModule.getModuleFromAlias(moduleRef);
        } 
        
        if (M.enumExists(s)) {
            Enum E = M.getEnum(s);
            E.isUsed = true;
            T = new EnumType(E, E.pos);
        } else if (M.structExists(s)) {
            Struct S = M.getStruct(s);
            S.isUsed = true;
            T = new StructType(S, S.pos);
        } else {
            handler.reportError(errors[40] + ": %", "'" + s + "'", type.pos);
            T = Environment.errorType;
        }
    
        return T;
    }

    private void unMurk(Decl decl) {
        MurkyType type = (MurkyType) decl.T;
        Type T = unMurk(type);
        decl.T = T;
    }

    private void unMurkArr(Decl ast) {
        ArrayType AT = (ArrayType) ast.T;
        Type innerT = unMurk((MurkyType) AT.t);
        AT.t = innerT;
    }

    private void unMurkPointer(Decl ast) {
        PointerType PT = (PointerType) ast.T;
        Type innerT = unMurk((MurkyType) PT.t);
        PT.t = innerT;
    }

    private void checkMurking(Decl ast) {
        if (ast.T.isMurky()) {
            unMurk(ast);
        } else if (ast.T.isArray() && ((ArrayType) ast.T).t.isMurky()) {
            unMurkArr(ast);
        } else if (ast.T.isPointer() && ((PointerType) ast.T).t.isMurky()) {
            unMurkPointer(ast);
        }
    }

    private Object visitVarDecl(Decl ast, Type existingType, Ident I, Expr E) {
        checkMurking(ast);
        existingType = ast.T;

        declareVariable(ast.I, ast);

        if (existingType.isVoid()) {
            handler.reportError(errors[3] + ": %", ast.I.spelling, ast.T.pos);
            existingType = Environment.errorType;
            return existingType;
        }

        if (existingType.isArray()) {
            Type iT = ((ArrayType) existingType).t;
            if (iT.isVoid()) {
                handler.reportError(errors[30] + ": %", ast.I.spelling, ast.T.pos);
                existingType = Environment.errorType;
                return existingType;
            }
        }

        I.visit(this, ast);

        // Unknown size of array
        if (E.isEmptyExpr()) {

            if (ast.T.isUnknown()) {
                handler.reportError(errors[44] + ": %", ast.I.spelling, ast.I.pos);
                existingType = Environment.errorType;
            }

            if (existingType.isArray() && ((ArrayType) existingType).length == -1) {
                handler.reportError(errors[34] + ": %", ast.I.spelling, ast.T.pos);
                existingType = Environment.errorType;
            }
            return existingType;
        }

        Type returnType = null;
        declaringLocalVar = true;
        if (existingType.isNumeric()) {
            currentNumericalType = existingType;
        }

        if (E.isDotExpr() || E.isIntOrDecimalExpr()) {
            try {
                E = (Expr) E.visit(this, ast);
            } catch (Exception e) {
                return Environment.errorType;
            }
            if (ast instanceof LocalVar L) {
                L.E = E;
            } else if (ast instanceof GlobalVar G) {
                G.E = E;
            }
            returnType = E.type;
        } else {
            returnType = (Type) E.visit(this, ast);
        }
        declaringLocalVar = false;
        currentNumericalType = null;

        // TODO: probably improve this, a bit hacky
        if (E.isCallExpr()) {
            CallExpr CE = (CallExpr) E;
            if (CE.I.spelling.equals("malloc")) {
                E.type = existingType;
            }
        }

        if (returnType == null || returnType.isError()) {
            return returnType;
        }   

        if (ast.T.isUnknown()) {
           ast.T = returnType;
           ast.T.parent = ast;
           return ast.T;
        }

        if (returnType != null && !existingType.assignable(returnType) && !returnType.isError()) {
            String message = "expected " + existingType + ", received " + returnType;
            handler.reportError(errors[5] + ": %", message, E.pos);
            existingType = Environment.errorType;
            return existingType;
        }

        // May need to cast
        Expr e2AST = checkCast(ast.T, E, null);
        if (ast instanceof LocalVar V) {
            V.E = e2AST;
        } else if (ast instanceof GlobalVar V) {
            V.E = e2AST;
        }
        return existingType;
    }

    private Expr checkCast(Type expectedT, Expr expr, AST parent) {

        if (expectedT.isPointer()) {
            if (((PointerType) expectedT).t.isAny()) {
                return expr;
            }
        }

        if (expectedT.isNumeric() && expr.type.isNumeric()) {
            if (expectedT.getMini().equals(expr.type.getMini())) {
                return expr;
            }
            CastExpr E = null;
            if (prioritiseIntTypes(expectedT, expr.type)) {
                // This means that the expectedT is of higher priority
                E = new CastExpr(expr, expr.type, expectedT, expr.pos, parent);
            } else {
                E = new CastExpr(expr, expectedT, expr.type, expr.pos, parent);
            }
            
            E.visit(this, null);
            return E;
        }

        // TODO: fix this, so lazy
        if (expectedT instanceof I8Type && expr.type instanceof I64Type) {
            CastExpr E = new CastExpr(expr, expr.type, expectedT, expr.pos);
            E.visit(this, null);
            return E;
        } else if (expectedT instanceof I64Type && expr.type instanceof I8Type) {
            CastExpr E = new CastExpr(expr, expr.type, expectedT, expr.pos);
            E.visit(this, null);
            return E;
        } else if (expectedT instanceof I64Type && expr.type instanceof EnumType) {
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
        E.visit(this, null);
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
        Type condT;
        
        if (ast.E.isDotExpr() || ast.E.isIntOrDecimalExpr()) {
            Expr e1 = (Expr) ast.E.visit(this, ast);
            condT = e1.type;
        } else {
            condT = (Type) ast.E.visit(this, ast);
        }
       
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
        if (!ast.E2.isEmptyExpr()) {
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
        Type conditionType;
        if (ast.E.isStructAccess() || ast.E.isIntOrDecimalExpr()) {
            ast.E = (Expr) ast.E.visit(this, ast);
            conditionType = ast.E.type;
        } else {
            conditionType = (Type) ast.E.visit(this, ast);
        }
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
        if (ast.E.isEmptyExpr() && !(this.currentFunctionType.isVoid())) {
            seenIncompatible = true;
            String message = "expected " + this.currentFunctionType.toString() + ", received void";
            handler.reportError(errors[6] + ": %", message, ast.E.pos);
        }
        Type conditionType;
        if (ast.E.isEmptyExpr()) {
            conditionType = new VoidType(new Position());
            ast.E.type = Environment.voidType;
        } else {
            if (this.currentFunctionType.isNumeric()) {
                currentNumericalType = this.currentFunctionType;
            }
            if (ast.E.isDotExpr() || ast.E.isIntOrDecimalExpr()) {
                ast.E = (Expr) ast.E.visit(this, ast);
                conditionType= ast.E.type;
            } else {
                conditionType= (Type) ast.E.visit(this, ast);
            }
            currentNumericalType = null;
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

    // True if first one takes priority
    // Assumes types are int types, and are unique
    private boolean prioritiseIntTypes(Type T1, Type T2) {
        String t1 = T1.getMini();
        String t2 = T2.getMini();
        int i1 = -1, i2 = -1;
        int index = 0;
        for (String s: numPriority) {
            if (s.equals(t1)) {
                i1 = index;
            } else if (s.equals(t2)) {
                i2 = index;
            }
            index++;
        }
        return i1 > i2;
    } 

    // Only to be used for primitive types
    private boolean isSameType(Type T1, Type T2) {
        return T1.getMini().equals(T2.getMini());
    }

    private String getPrefix(Type T) {
        if (T.isI8()) {
            return "i8";
        } else if (T.isI32()) {
            return "i32";
        } else if (T.isI64() || T.isEnum()) {
            return "i64";
        } else if (T.isF32()) {
            return "f32";
        } else if (T.isF64()) {
            return "f64";
        } else if (T.isBoolean()) {
            return "b";
        }
        return "TODO";
    }


    public Object visitBinaryExpr(BinaryExpr ast, Object o) {
        ast.O.visit(this, ast);
        Type t1, t2;
        if (ast.E1.isDotExpr() || ast.E1.isIntOrDecimalExpr()) {
            ast.E1 = (Expr) ast.E1.visit(this, o);
            t1 = ast.E1.type;
        } else {
            t1 = (Type) ast.E1.visit(this, o);
        }

        if (ast.E2.isDotExpr() || ast.E2.isIntOrDecimalExpr()) {
            ast.E2 = (Expr) ast.E2.visit(this, o);
            t2 = ast.E2.type;
        } else {
            t2 = (Type) ast.E2.visit(this, o);
        }

        boolean v1Numeric = t1.isNumeric() || t1.isEnum() || t1.isError();
        boolean v2Numeric = t2.isNumeric() || t2.isEnum() || t2.isError();

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
            if (isSameType(t1, t2)) {
                ast.O.spelling = getPrefix(t1) + ast.O.spelling;
                return ast.type;
            }

            // Not including enum types!
            assert(t1.isNumeric() && t2.isNumeric());
            if (prioritiseIntTypes(t1, t2)) {
                ast.O.spelling = getPrefix(t1) + ast.O.spelling;
                ast.E2 = new CastExpr(ast.E2, t2, t1, ast.E2.pos, ast);
            } else {
                ast.O.spelling = getPrefix(t2) + ast.O.spelling;
                ast.E1 = new CastExpr(ast.E1, t1, t2, ast.E1.pos, ast);
            }
            return ast.type;
        }

        if (isSameType(t1, t2)) {
            ast.O.spelling = getPrefix(t1) + ast.O.spelling;
            ast.type = t1;
            return ast.type;
        }

        assert(t1.isNumeric() && t2.isNumeric());   

        if (prioritiseIntTypes(t1, t2)) {
            ast.O.spelling = getPrefix(t1) + ast.O.spelling;
            ast.E2 = new CastExpr(ast.E2, t2, t1, ast.E2.pos, ast);
            ast.type = t1;
        } else {
            ast.O.spelling = getPrefix(t2) + ast.O.spelling;
            ast.E1 = new CastExpr(ast.E1, t1, t2, ast.E1.pos, ast);
            ast.type = t2;
        }

        return ast.type;
    }

    public Object visitUnaryExpr(UnaryExpr ast, Object o) {
        ast.O.visit(this, ast);
        Type eT;
        if (ast.E.isDotExpr() || ast.E.isIntOrDecimalExpr()) {
            ast.E = (Expr) ast.E.visit(this, o);
            eT = ast.E.type;
        } else {
            eT = (Type) ast.E.visit(this, o);
        }

        if (eT.isError()) {
            return ast.type;
        }

        switch (ast.O.spelling) {
            case "+", "-" -> {
                if (!eT.isNumeric()) {
                    handler.reportError(errors[8], "", ast.O.pos);
                    ast.type = Environment.errorType;
                    break;
                }
                ast.O.spelling = getPrefix(eT) + ast.O.spelling;
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
                if (!ast.E.isVarExpr()) {
                    handler.reportError(errors[27], "", ast.O.pos);
                    break;
                }
                ast.type = ((PointerType) eT).t;
            }
            case "&" -> { // address of operator
                if (!ast.E.isVarExpr()) {
                    handler.reportError(errors[27], "", ast.O.pos);
                    break;
                }
                VarExpr VE = (VarExpr) ast.E;
                SimpleVar SV = (SimpleVar) VE.V;
                Decl decl = idTable.retrieve(SV.I.spelling);
                if (decl == null) {
                    decl = mainModule.getVar(SV.I.spelling);
                }
                if (!decl.isMut) {
                    handler.reportError(errors[28], "", ast.O.pos);
                    break;
                }
                ast.type = new PointerType(decl.pos, decl.T);
            }
        }
        return ast.type;
    }

    public Object visitDeclList(DeclList ast, Object o) {
        ast.D.visit(this, null);
        ast.DL.visit(this, null);
        return null;
    }

    public Object visitStmtList(StmtList ast, Object o) {
        baseStatementCounter += 1;
        if (ast.S.isCompoundStmt()) {
            idTable.openScope();
            ast.S.visit(this, o);
            idTable.closeScope();
        } else {
            ast.S.visit(this, o);
        }
        // Unreached statements
        if (ast.S.isReturnStmt() && ast.SL.isStmtList()) {
            handler.reportMinorError(errors[18], "", ast.SL.pos);
        }
        ast.SL.visit(this, o);
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

    public Object visitI64Expr(I64Expr ast, Object o) {
        ast.type = Environment.i64Type;
        return ast.type;
    }

    public Object visitI32Expr(I32Expr ast, Object o) {
        ast.type = Environment.i32Type;
        return ast.type;
    }

    public Object visitIntExpr(IntExpr ast, Object o) {
        
        Expr E = null;
        if (currentNumericalType == null || currentNumericalType.isI64() || currentNumericalType.isFloat()) {
            E = new I64Expr(ast.IL, ast.pos);
        } else if (currentNumericalType.isI32()) {
            E = new I32Expr(ast.IL, ast.pos);
        } else if (currentNumericalType.isI8()) {
            Expr inner = new I64Expr(ast.IL, ast.pos);
            inner.visit(this, o);
            E = new CastExpr(inner, inner.type, Environment.i8Type, dummyPos);
        }
         
        if (!E.isIntExpr()) {
            E.visit(this, o);
            E.parent = ast.parent;
        } else {
            E.type = Environment.i64Type;
        }

        if (currentNumericalType != null) {
            E = checkCast(currentNumericalType, E, ast);
        }

        return E;
    }

    public Object visitDecimalExpr(DecimalExpr ast, Object o) {
        
        Expr E = null;
        if (currentNumericalType == null || currentNumericalType.isF64()) {
            E = new F64Expr(ast.DL, ast.pos);
        } else if (currentNumericalType.isF32()) {
            E = new F32Expr(ast.DL, ast.pos);
        } else {
            System.out.println("UNREACHABLE: VISIT DECIMAL EXPR");
            return null;
        }

        E.parent = ast.parent;
        E.visit(this, o);
        return E;
    }

    public Object visitIntLiteral(IntLiteral ast, Object o) {
        return Environment.i64Type;
    }

    public Object visitI64Type(I64Type ast, Object o) {
        return Environment.i64Type;
    }

    public Object visitI32Type(I32Type ast, Object o) {
        return Environment.i32Type;
    }

    public Object visitArgList(Args ast, Object o) {
        List PL = (List) o;
        Type expectedType = ((ParaList) PL).P.T;

        if (expectedType.isNumeric()) {
            currentNumericalType = expectedType;
        }
        if (ast.E.isDotExpr() || ast.E.isIntOrDecimalExpr()) {
            ast.E = (Expr) ast.E.visit(this, o);
        }
        currentNumericalType = null;

        ast.E = checkCast(expectedType, ast.E, ast);
        ast.EL.visit(this, ((ParaList) PL).PL);
        return null;
    }

    public Object visitParaList(ParaList ast, Object o) {
        ast.P.visit(this, null);
        ast.PL.visit(this, null);
        return null;
    }

    public Object visitParaDecl(ParaDecl ast, Object o) {
        checkMurking(ast);
        declareVariable(ast.I, ast);
        if (ast.T.isVoid()) {
            handler.reportError(errors[3] + ": %", ast.I.spelling, ast.I.pos);
        }
        if (ast.T.isArray()) {
            Type iT = ((ArrayType) ast.T).t;
            if (iT.isVoid()) {
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

        if (decl.isGlobalVar()) {
            if (mainModule.varExists(ident.spelling)) {
                handler.reportError(errors[2] + ": %", ident.spelling, ident.pos);
            }
            mainModule.addGlobalVar((GlobalVar) decl);
        } else {
            idTable.insert(ident.spelling, decl.isMut, decl);
        }

        ident.visit(this, null);
    }

    public Object visitVarExpr(VarExpr ast, Object o) {
        // Assumes that when a function parameter is declared mutable
        // It actually is mutated
        Var V = ast.V;
        String s = ((SimpleVar) V).I.spelling;
        
        // If it's local
        Decl decl = idTable.retrieve(s);
        if (decl != null) {
            decl.isReassigned = true;
        }

        // If it's global
        decl = mainModule.getVar(s);
        if (decl != null) {
            decl.isReassigned = true;
        }

        ast.type = (Type) ast.V.visit(this, o);
        return ast.type;
    }

    public Object visitSimpleVar(SimpleVar ast, Object o) {

        if (declaringLocalVar) {
            ast.setDeclaringLocalVar();
        }

        if (ast.I.isModuleAccess) {
            if (ast.I.spelling.equals("$")) {
                handler.reportError(errors[4] + ": %", ast.I.spelling, ast.I.pos);
                return Environment.errorType;
            }

            String moduleAlias = ast.I.module.get();
            String message = moduleAlias + "::" + ast.I.spelling;

            if (!mainModule.aliasExists(moduleAlias)) {
                handler.reportError(errors[63] + ": %", message, ast.I.pos);
                return Environment.errorType;
            } 

            Module specificModule = mainModule.getModuleFromAlias(moduleAlias);
            if (!specificModule.varExists(ast.I.spelling)) {
                handler.reportError(errors[66] + ": %", message, ast.I.pos);
                return Environment.errorType;
            }

            GlobalVar G = specificModule.getVar(ast.I.spelling);
            if (!G.isExported) {
                message = "variable '" + ast.I.spelling + "' in module '" + moduleAlias + "'";
                handler.reportError(errors[64] + ": %", message, ast.I.pos);
                return Environment.errorType;
            }
            ast.I.decl = G;
            G.isUsed = true;
            return G.T;
        }

        if (ast.I.spelling.equals("$") && !validDollar) {
            handler.reportError(errors[24] + ": %", ast.I.spelling, ast.I.pos);
            return Environment.errorType;
        } else if (ast.I.spelling.equals("$")) {
            return Environment.i64Type;
        }

        Decl decl = idTable.retrieve(ast.I.spelling);
        if (decl == null) {
            decl = mainModule.getVar(ast.I.spelling);
            if (decl == null) {
                if (mainModule.functionWithNameExists(ast.I.spelling)) {
                    handler.reportError(errors[9], "", ast.I.pos);
                } else {
                    handler.reportError(errors[4] + ": %", ast.I.spelling, ast.I.pos);
                }
                return Environment.errorType;
            }
        }

        ast.I.decl = decl;
        boolean isFnCall = ast.parent.parent.isArgs();
        boolean isStructDecl = ast.parent.parent.isStructArgs();
        if (decl.T.isArray() && !isFnCall && !isStructDecl) {
            handler.reportError(errors[31] + ": %", ast.I.spelling, ast.I.pos);
            return Environment.errorType;
        }

        if (decl.isFunction()) {
            handler.reportError(errors[9], "", ast.I.pos);
            return Environment.errorType;
        } else if (decl.isGlobalVar() || decl.isLocalVar()) {
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
            if (ast.I1.get().isDotExpr() || ast.I1.get().isIntOrDecimalExpr()) {
                ast.I1 = Optional.of((Expr) ast.I1.get().visit(this, o));
                T1 = ast.I1.get().type;
            } else {
                T1 = (Type) ast.I1.get().visit(this, o);
            }
            if (!T1.isI64()) {
                handler.reportError(errors[25], "", ast.pos);
            }
        }
        if (ast.I2.isPresent()) {
            if (ast.I2.get().isDotExpr() || ast.I2.get().isIntOrDecimalExpr()) {
                ast.I2 = Optional.of((Expr) ast.I2.get().visit(this, o));
                T2 = ast.I2.get().type;
            } else {
                T2 = (Type) ast.I2.get().visit(this, o);
            }
            if (!T2.isI64()) {
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

    public Object visitDecimalLiteral(DecimalLiteral ast, Object o) {
        return Environment.f32Type;
    }

    public Object visitF32Type(F32Type ast, Object o) {
        return Environment.f32Type;
    }

    public Object visitF64Type(F64Type ast, Object o) {
        return Environment.f64Type;
    }

    public Object visitF32Expr(F32Expr ast, Object o) {
        ast.type = Environment.f32Type;
        return ast.type;
    }

    public Object visitF64Expr(F64Expr ast, Object o) {
        ast.type = Environment.f64Type;
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

        if (ast.AL.isEmptyArgList()) {
            handler.reportError(errors[44] + ": %", "array type cannot be deduced from 0 elements", ast.pos);
            return Environment.errorType;
        }

        int length = 0;
        Args args = (Args) ast.AL;
        Type T;
        if (args.E.isDotExpr() || args.E.isIntOrDecimalExpr()) {
            args.E = (Expr) args.E.visit(this, o);
            T = args.E.type;
        } else {
            T = (Type) args.E.visit(this, o);
        }

        length++;
        if (args.EL.isEmptyArgList()) {
            return new ArrayType(ast.pos, T, length);
        }

        args = (Args) args.EL;
        while (true) {
            Type t;
            if (T.isNumeric()) {
                currentNumericalType = T;
            }
            if (args.E.isDotExpr() || args.E.isIntOrDecimalExpr()) {
                args.E = (Expr) args.E.visit(this, o);
                t = args.E.type;
            } else {
                t = (Type) args.E.visit(this, o);
            }
            currentNumericalType = null;

            if (!t.assignable(T)) {
                String message = "expected " + T + ", received " + t + " at position " + length;
                handler.reportError(errors[33] + ": %", message, ast.pos);
            }

            args.E = checkCast(T, ((Args) args).E, ast);
            args.E.parent = ast.AL;
            length++;
            if (args.EL.isEmptyArgList()) {
                break;
            }
            args = (Args) args.EL;
        }

        return new ArrayType(ast.pos, T, length);
    }

    public Object visitArrayInitExpr(ArrayInitExpr ast, Object o) {
        
        Type expectedT;
        if (ast.parent.isStructArgs()) {
            expectedT = (Type) o;
        } else {
            expectedT = ((Decl) o).T;
        }

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

        if (ast.AL.isEmptyArgList()) {
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
            Type t;
            if (iT.isNumeric()) {
                currentNumericalType = iT;
            }
            if (args.E.isDotExpr() || args.E.isIntOrDecimalExpr()) {
                args.E = (Expr) args.E.visit(this, o);
                t = args.E.type;
            } else {
                t = (Type) args.E.visit(this, o);
            }
            currentNumericalType = null;

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

            if (args.EL.isEmptyArgList()) {
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

        if (declaringLocalVar) {
            ast.setDeclaringLocalVar();
        }

        Decl binding = (Decl) ast.I.visit(this, o);
        if (binding == null) {
            if (idTable.retrieve(ast.I.spelling) != null || mainModule.varExists(ast.I.spelling)
                || mainModule.functionWithNameExists(ast.I.spelling)) {
                handler.reportError(errors[32] + ": %", ast.I.spelling, ast.I.pos);
            } else {
                handler.reportError(errors[4] + ": %", ast.I.spelling, ast.I.pos);
            }
            return Environment.errorType;
        }

        if (binding.isFunction() || !binding.T.isArray()) {
            handler.reportError(errors[32] + ": %", ast.I.spelling, ast.I.pos);
            return Environment.errorType;
        }

        if (!ast.isLHSOfAssignment) {
            binding.isUsed = true;
        } else {
            binding.isReassigned = true;
        }

        // make sure that the index is of int type
        Type T;
        currentNumericalType = Environment.i32Type;
        if (ast.index.isDotExpr() || ast.index.isIntOrDecimalExpr()) {
            ast.index = (Expr) ast.index.visit(this, o);
            T = ast.index.type;
        } else {
            T = (Type) ast.index.visit(this, o);
        }
        currentNumericalType = null;

        if (!T.isInteger()) {
            handler.reportError(errors[37] + ": %", ast.I.spelling, ast.I.pos);
            return Environment.errorType;
        }

        if (!T.isI32()) {
            ast.index = new CastExpr(ast.index, T, Environment.i32Type, ast.index.pos, ast);
        }

        ast.type = binding.T;
        return ((ArrayType) binding.T).t;
    }

    public Object visitI8Type(I8Type ast, Object o) {
        return Environment.i8Type;
    }

    public Object visitCharLiteral(CharLiteral ast, Object o) {
        return Environment.i8Type;
    }

    public Object visitCharExpr(CharExpr ast, Object o) {
        int l = ast.CL.spelling.length();
        if (l != 1) {
            String m = "received '" + ast.CL.spelling + "'";
            handler.reportMinorError(errors[38] + ": %", m, ast.CL.pos);
            return Environment.errorType;
        }
        ast.type = Environment.i8Type;
        return ast.type;
    }

    public Object visitCallExpr(CallExpr ast, Object o) {
        if (inMain && ast.I.spelling.equals("main")) {
            handler.reportError(errors[17], "", ast.I.pos);
            return Environment.errorType;
        }

        boolean isFreeCall = !ast.I.isModuleAccess && ast.I.spelling.equals("free");

        String TL;
        if (ast.TypeDef == null) {
            TL = genTypes(ast.AL, o);
            ast.setTypeDef(TL);
        }

        if (ast.isLibC) {
            if (!modules.libCFunctionExists(ast.I.spelling)) {
                handler.reportError(errors[68] + ": %", ast.I.spelling, ast.I.pos);
                return Environment.errorType;
            }
        }
        else if (ast.I.isModuleAccess) {
            String moduleAlias = ast.I.module.get();
            String message = moduleAlias + "::" + ast.I.spelling;
            if (!mainModule.aliasExists(moduleAlias)) {
                message = moduleAlias + "::" + ast.I.spelling;
                handler.reportError(errors[63] + ": %", message, ast.I.pos);
                ast.type = Environment.errorType;
                return ast.type;
            } else {
                Module specificModule = mainModule.getModuleFromAlias(moduleAlias);

                if (!specificModule.functionExists(ast.I.spelling + "." + ast.TypeDef)) {
                    System.out.println(ast.I.spelling + "." + ast.TypeDef);
                    specificModule.printAllFunctions();
                    if (specificModule.functionWithNameExists(ast.I.spelling)) {
                        handler.reportError(errors[43] + ": %", message, ast.I.pos);
                    } else {
                        message = "function '" + ast.I.spelling + "' in module '" + moduleAlias + "'";
                        handler.reportError(errors[65] + ": %", message, ast.I.pos);
                    }
                    ast.type = Environment.errorType;
                    return ast.type;
                } else {
                    Function function = specificModule.getFunction(ast.I.spelling + "." + ast.TypeDef);
                    if (!function.isExported) {
                        message = "function '" + ast.I.spelling + "' in module '" + moduleAlias + "'";
                        handler.reportError(errors[64] + ": %", message, ast.I.pos);
                        ast.type = Environment.errorType;
                        return ast.type;
                    }
                    ast.I.decl = function;
                    function.setUsed();
                    ast.AL.visit(this, function.PL);
                    ast.type = function.T;
                    return function.T;
                }
            }
        }
        else if (!mainModule.functionExists(ast.I.spelling + "." + ast.TypeDef) && !isFreeCall) {
            if (idTable.retrieve(ast.I.spelling) != null || mainModule.varExists(ast.I.spelling)) {
                handler.reportError(errors[10] + ": %", ast.I.spelling, ast.I.pos);
            } else if (mainModule.functionWithNameExists(ast.I.spelling)) {
                handler.reportError(errors[43] + ": %", ast.I.spelling, ast.I.pos);
            } else {
                handler.reportError(errors[4] + ": %", ast.I.spelling, ast.I.pos);
            }
            ast.type = Environment.errorType;
            return ast.type;
        }

        Function function;
        if (isFreeCall) {
            function = mainModule.getFunction("free.PA");
        } else if (ast.isLibC) {
            function = modules.getLibCFunction(ast.I.spelling);
        } else {
            function = mainModule.getFunction(ast.I.spelling + "." + ast.TypeDef);
        }
        ast.I.decl = function;
        function.setUsed();
        ast.AL.visit(this, function.PL);
        ast.type = function.T;
        return function.T;
    }

    private String genTypes(List PL, Object o) {
        List head = PL;
        ArrayList<String> options = new ArrayList<>();
        while (!head.isEmptyArgList()) {
            Expr D = ((Args) head).E;
            Type t;
            if (D.isDotExpr() || D.isIntOrDecimalExpr()) {
                Expr E = (Expr) D.visit(this, o);
                t = E.type;
            } else {
                t = (Type) D.visit(this, o);
            }
            options.add(t.getMini());
            head = ((Args) head).EL;
        }
        return String.join("_", options);
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
        ast.type = new PointerType(ast.pos, new I8Type(ast.pos));
        return ast.type;
    }

    public Object visitEnumExpr(EnumExpr ast, Object o) {

        if (ast.Type.isModuleAccess) {
            // Assert at this point to know the alias is already valid
            String alias = ast.Type.module.get();
            String message = "";
            Module M = mainModule.getModuleFromAlias(alias);
            Enum E = M.getEnum(ast.Type.spelling);
            if (!E.isExported) {
                message = "enum '" + ast.Type.spelling + "' in module '" + alias + "'";
                handler.reportError(errors[64] + ": %", message, ast.Type.pos);
                return Environment.errorType;
            }

            EnumType ET = new EnumType(E, E.pos);
            ast.type = ET;
            return ET;
        }

        if (!mainModule.enumExists(ast.Type.spelling)) {
            handler.reportError(errors[40] + ": %", ast.Type.spelling, ast.pos);
            return Environment.errorType;
        }

        Enum E = mainModule.getEnum(ast.Type.spelling);
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

    public Object visitStructAccessList(StructAccessList ast, Object o) {
        Struct ref = (Struct) o;
        Optional<StructElem> elem = ref.getElem(ast.SA.spelling);
        if (elem.isEmpty()) {
            String message = "key " + ast.SA.spelling + " on struct " + ref.I.spelling;
            handler.reportError(errors[58] + ": %", message, ast.pos);
            return Environment.errorType;
        }

        if (!elem.get().isMut && isStructLHS) {
            String message = "field '" + ast.SA.spelling + "'";
            handler.reportError(errors[60] + ": %", message, ast.pos);
            return Environment.errorType;
        }

        if (ast.arrayIndex.isPresent()) {
            if (!elem.get().T.isArray()) {
                String message = "key " + ast.SA.spelling + " on struct " + ref.I.spelling;
                handler.reportError(errors[59] + ": %", message, ast.pos);
                return Environment.errorType;
            }

            if (ast.arrayIndex.get().isDotExpr() || ast.arrayIndex.get().isIntOrDecimalExpr()) {
                ast.arrayIndex = Optional.of((Expr) ast.arrayIndex.get().visit(this, o));
            } else {
                ast.arrayIndex.get().visit(this, o);
            }
        }

        if (ast.SAL.isEmptyStructAccessList()) {
            // Reached the end
            if (ast.arrayIndex.isPresent()) {
                return ((ArrayType) elem.get().T).t;
            }
            return elem.get().T;
        }

        // Need to make sure the subtype is a struct itself
        if (!(elem.get().T instanceof StructType TA)) {
            String message = "key " + ast.SA.spelling + " on struct " + ref.I.spelling;
            handler.reportError(errors[59] + ": %", message, ast.pos);
            return Environment.errorType;
        } else {
            ast.ref = TA.S;
        }

        if (ast.arrayIndex.isPresent()) {
            Type innerType = ((ArrayType) elem.get().T).t;
            Struct ref2 = ((StructType) innerType).S;
            return ast.SAL.visit(this, ref2);
        } else {
            Struct ref2 = ((StructType) elem.get().T).S;
            return ast.SAL.visit(this, ref2);
        }
    }

    // need to do standard verifications
    // ascertain a type based on the furthest right access
    // e.g. x.b.z
    //          ^- that one
    public Object visitStructAccess(StructAccess ast, Object o) {
        // Passing through 'o' as the struct ref
        Type rightMostType = (Type) ast.L.visit(this, ast.ref);
        ast.type = rightMostType;
        return rightMostType;
    }
    public Object visitStructArgs(StructArgs ast, Object o) {
        StructList L = (StructList) o;
        Type expectedType = L.S.T;
        Type realType;
        if (expectedType.isNumeric()) {
            currentNumericalType = expectedType;
        }
        if (ast.E.isDotExpr() || ast.E.isIntOrDecimalExpr()) {
            ast.E = (Expr) ast.E.visit(this, o);
            realType = ast.E.type;
        } else {
            realType = (Type) ast.E.visit(this, L.S.T);
        }
        currentNumericalType = null; 

        ast.E.type = realType;
        if (!realType.assignable(expectedType)) {
            String message = "member '" + L.S.I.spelling + "' should be of type " +
                expectedType + ", but received " + realType;
            handler.reportError(errors[52] + ": %", message, ast.pos);
        }
        if (L.SL.isStructList()) {
            ast.SL.visit(this, L.SL);
        }
        return null;
    }

    public Object visitStructExpr(StructExpr ast, Object o) {
        String name = ast.I.spelling;
        Struct DS = null;
        if (ast.I.isModuleAccess) {
            String moduleAlias = ast.I.module.get();
            String message = moduleAlias + "::" + ast.I.spelling;

            if (!mainModule.aliasExists(moduleAlias)) {
                message = moduleAlias + "::" + ast.I.spelling;
                handler.reportError(errors[63] + ": %", message, ast.I.pos);
                return Environment.errorType;
            }

            Module specificModule = mainModule.getModuleFromAlias(moduleAlias);
            if (!specificModule.structExists(name)) {
                message = "struct '" + ast.I.spelling + "' in module '" + moduleAlias + "'";
                handler.reportError(errors[67] + ": %", message, ast.I.pos);
                return Environment.errorType;
            }

            DS = specificModule.getStruct(name);
        } else {
            if (!mainModule.structExists(name)) {
                handler.reportError(errors[40] + ": %", "'" + name + "'", ast.I.pos);
                return Environment.errorType;
            }
            DS = mainModule.getStruct(name);
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
            || o instanceof DerefExpr || o instanceof DotExpr;
    }

    public Object visitAssignmentExpr(AssignmentExpr ast, Object o) {
        if (ast.LHS.isVarExpr()) {
            VarExpr VE = (VarExpr) ast.LHS;
            Decl D = idTable.retrieve(((SimpleVar) VE.V).I.spelling);

            if (D == null) {
                if (mainModule.varExists(((SimpleVar) VE.V).I.spelling)) {
                    D = mainModule.getVar(((SimpleVar) VE.V).I.spelling);
                    if (D.T.isArray()) {
                        handler.reportError(errors[36],  "", ast.LHS.pos);
                        return Environment.errorType;
                    }
                }
            }

            if (D != null && D.T.isArray()) {
                handler.reportError(errors[36],  "", ast.LHS.pos);
                return Environment.errorType;
            }
        }

        if (!validLHS(ast.LHS)) {
            String message = "must be a variable, struct access or an array index";
            handler.reportError(errors[54] + ": %", message, ast.LHS.pos);
            return Environment.errorType;
        }

        Type realType, expectedType;
        if (ast.LHS.isDotExpr()) {
            isStructLHS = true;
            ast.LHS = (Expr) ast.LHS.visit(this, o);
            isStructLHS = false;
            expectedType = ast.LHS.type;
            if (expectedType.isError()) {
                return expectedType;
            }
        } else {
            expectedType = (Type) ast.LHS.visit(this, o);
        }

        declaringLocalVar = true;
        if (expectedType.isNumeric()) {
            currentNumericalType = expectedType;
        }
        if (ast.RHS.isDotExpr() || ast.RHS.isIntOrDecimalExpr()) {
            ast.RHS = (Expr) ast.RHS.visit(this, o);
            realType = ast.RHS.type;
        } else {
            realType = (Type) ast.RHS.visit(this, o);
        }
        currentNumericalType = null;
        declaringLocalVar = false;

        if (ast.RHS.isCallExpr()) {
            CallExpr CE = (CallExpr) ast.RHS;   
            if (CE.I.spelling.equals("malloc")) {
                ast.RHS.type = ast.LHS.type;
            }
        }

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
        return ast.type;
    }

    // Currently operating under the presumption there's no pointer accesses
    public Object visitDotExpr(DotExpr ast, Object o) {

        Expr errorExpr = new EmptyExpr(ast.pos);
        errorExpr.type = Environment.errorType;
        Module M = mainModule;
        boolean isExternalModule = false;
        String message = "";

        if (ast.I.isModuleAccess) {
            String alias = ast.I.module.get();
            if (!mainModule.aliasExists(alias)) {
                handler.reportError(errors[63] + ": %", "'" + alias + "'", ast.I.pos);
                return errorExpr;
            }
            M = mainModule.getModuleFromAlias(alias);
            isExternalModule = true;
        }


        // First check if the identifier is an enum name
        if (M.enumExists(ast.I.spelling)) {
            Enum E = M.getEnum(ast.I.spelling);
            E.isUsed = true;

            DotExpr innerE = (DotExpr) ast.E;
            if (!innerE.E.isEmptyExpr()) {
                handler.reportError(errors[55], "", ast.pos);
                return Environment.errorType;
            }
            if (ast.arrayIndex.isPresent()) {
                handler.reportError(errors[32], "", ast.pos);
                return Environment.errorType;
            }

            EnumExpr newEnum = new EnumExpr(ast.I, innerE.I, ast.pos);
            newEnum.visit(this, o);
            return newEnum;
        } else {
            // Assumption is now we have an attempted struct access
            Decl d;
            if (!isExternalModule) {
                boolean isGlobalVar = M.varExists(ast.I.spelling);
                boolean isLocalVar = idTable.retrieve(ast.I.spelling) != null;
                if (!(isLocalVar || isGlobalVar)) {
                    handler.reportError(errors[56], "", ast.pos);
                    return errorExpr;
                }

                if (isLocalVar) {
                    d = idTable.retrieve(ast.I.spelling);
                } else {
                    d = M.getVar(ast.I.spelling);
                }
            } else {
                // Needs to be a global variable
                if (!M.varExists(ast.I.spelling)) {
                    message = ast.I.module.get() + "::" + ast.I.spelling;
                    handler.reportError(errors[66] + ": %", message, ast.I.pos);
                    return errorExpr;
                }
                GlobalVar G = M.getVar(ast.I.spelling);
                if (!G.isExported) {
                    message = "variable '" + ast.I.spelling + "' in module '" + ast.I.module.get() + "'";
                    handler.reportError(errors[64] + ": %", message, ast.I.pos);
                    return Environment.errorType;
                }
                d = G;
            }

            if (!d.isMut && isStructLHS) {
                handler.reportError(errors[20] + ": %", "'" + ast.I.spelling + "'", ast.pos);
                return errorExpr;
            }

            Ident varName = d.I;
            Struct ref = null;
            if (d.T.isArray()) {
                if (ast.arrayIndex.isEmpty()) {
                    handler.reportError(errors[32], "", ast.pos);
                    return errorExpr;
                }
                Type indexType;
                Type innerT = ((ArrayType) d.T).t;
                currentNumericalType = Environment.i32Type;
                if (ast.arrayIndex.get().isDotExpr() || ast.arrayIndex.get().isIntOrDecimalExpr()) {
                    Expr E2 = (Expr) ast.arrayIndex.get().visit(this, o);
                    ast.arrayIndex = Optional.of(E2);
                    indexType = E2.type;
                } else {
                    indexType = (Type) ast.arrayIndex.get().visit(this, o);
                }
                currentNumericalType = null;

                if (!indexType.isInteger()) {
                    handler.reportError(errors[37], "", ast.pos);
                    return errorExpr;
                }

                if (!innerT.isStruct()) {
                    handler.reportError(errors[57] + ": %", varName.spelling, ast.pos);
                    return errorExpr;
                }
                ref = ((StructType) innerT).S;
            }
            else if (!d.T.isStruct()) {
                handler.reportError(errors[57] + ": %", varName.spelling, ast.pos);
                return errorExpr;
            } else {
                ref = ((StructType) d.T).S;
            }
            StructAccessList SL = generateStructAccessList((DotExpr) ast.E);
            StructAccess SA = new StructAccess(ref, varName, SL, ast.pos, ast.arrayIndex, d.T);
            SA.parent = ast.parent;
            SA.isLHSOfAssignment = ast.isLHSOfAssignment;
            SA.visit(this, o);
            return SA;
        }
    }

    public StructAccessList generateStructAccessList(DotExpr ast) {
        if (ast.E instanceof EmptyExpr) {
            return new StructAccessList(ast.I, new EmptyStructAccessList(ast.pos), ast.pos, ast.arrayIndex);
        }
        return new StructAccessList(ast.I, generateStructAccessList((DotExpr) ast.E), ast.pos, ast.arrayIndex);
    }


    public Object visitExprStmt(ExprStmt ast, Object o) {
        if (!(ast.E.isAssignmentExpr() || ast.E.isCallExpr())) {
            System.out.println(ast.E);
            System.out.println("Should never be reached");
        }
        ast.E.visit(this, o);
        return null;
    }

    public Object visitStringLiteral(StringLiteral ast, Object o) {
        return new PointerType(ast.pos, new I8Type(ast.pos));
    }

    public Object visitTypeOfExpr(TypeOfExpr ast, Object o) {
        if (ast.E.isDotExpr() || ast.E.isIntOrDecimalExpr()) {
            ast.E = (Expr) ast.E.visit(this, o);
        } else {
            ast.E.visit(this, o);
        }
        Type t = ast.E.type;
        if (t.isArray()) {
            t = ((ArrayType) t).t;
        }
        StringLiteral SL = new StringLiteral(t.toString(), ast.pos);
        ast.SE = new StringExpr(SL, SL.pos);
        ast.SE.parent = ast;
        ast.SE.visit(this, o);
        return Environment.charPointerType;
    }

    public Object visitSizeOfExpr(SizeOfExpr ast, Object o) {
        if (ast.varExpr.isPresent()) {
            // Make sure the variable exists
            String name = ((SimpleVar) ast.varExpr.get().V).I.spelling;

            if (mainModule.enumExists(name)) {
                Enum E = mainModule.getEnum(name);
                E.isUsed = true;
                ast.varExpr = Optional.empty();
                ast.typeV = Optional.of(new EnumType(E, E.pos));
            } else if (mainModule.structExists(name)) {
                Struct S = mainModule.getStruct(name);
                S.isUsed = true;
                ast.varExpr = Optional.empty();
                ast.typeV = Optional.of(new StructType(S, S.pos));
            } else {
                Decl d = idTable.retrieve(name);

                if (d == null) {
                    d = mainModule.getVar(name);
                    if (d == null) {
                        handler.reportError(errors[4] + ": %", name, ast.pos);
                        return Environment.errorType;
                    }
                }
                ast.varType = d.T;
            }

        } else {
            assert(ast.typeV.isPresent());
            Type T = ast.typeV.get();
            if (T.isMurky()) {
                ast.typeV = Optional.of(unMurk((MurkyType) T));
            } else if (T.isArray() && ((ArrayType) T).t.isMurky()) {
                MurkyType MT = (MurkyType) ((ArrayType)T).t;
                Type T2 = unMurk(MT);
                ((ArrayType) T).t = T2;
            }

        }
        return ast.type;
    }

    public Object visitModule(Module ast, Object o) {
        return null;
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

    public Object visitEnum(Enum ast, Object o) {
        return null;
    }

    public Object visitMurkyType(MurkyType ast, Object o) {
        return null;
    }

    public Object visitEnumType(EnumType ast, Object o) {
        return null;
    }

    public Object visitOperator(Operator ast, Object o) {
        return null;
    }

    public Object visitEmptyExpr(EmptyExpr ast, Object o) {
        return null;
    }

    public Object visitEmptyStmt(EmptyStmt ast, Object o) {
        return null;
    }

    public Object visitEmptyStructAccessList(EmptyStructAccessList ast, Object o) {
        return null;
    }

    public Object visitEmptyArgList(EmptyArgList ast, Object o) {
        return null;
    }

    public Object visitCastExpr(CastExpr ast, Object o) {
        ast.type = ast.tTo;
        return ast.tTo;
    }

    public Object visitEmptyCompStmt(EmptyCompStmt ast, Object o) {
        return null;
    }

    public Object visitEmptyDeclList(EmptyDeclList ast, Object o) {
        return null;
    }

    public Object visitEmptyParaList(EmptyParaList ast, Object o) {
        return null;
    }

    public Object visitEmptyStmtList(EmptyStmtList ast, Object o) {
        return null;
    }

    public Object visitImportStmt(ImportStmt ast, Object o) {
        Path basePath = Paths.get(currentFileName).resolve(Paths.get("..")).normalize();
        Path relativePath = Paths.get(ast.path.SL.spelling);
        Path finalPath = basePath.resolve(relativePath).normalize();
        String fileName = finalPath.toString();
        File file = new File(fileName);
        if (!file.exists() || (file.exists() && file.isDirectory())) {
            handler.reportError(errors[61] + ": %", "'" + ast.path.SL.spelling + "'", ast.path.SL.pos);
            return null;
        }

        if (mainModule.importedFileExists(fileName)) {
            handler.reportError(errors[62] + ": %", "'" + ast.path.SL.spelling + "'", ast.path.SL.pos);
            return null;
        }

        if (modules.moduleExists(fileName)) {
            mainModule.addImportedFile(modules.getModule(fileName), ast.ident.spelling);
            return null;
        }

        ErrorHandler newHandler = new ErrorHandler(fileName, handler.isQuiet);
        Lex lexer = new Lex(new MyFile(fileName));
        ArrayList<Token> tokens = lexer.getTokens();
        Parser parser = new Parser(tokens, newHandler);
        AST currentAST = null;
        try {
            currentAST = parser.parseProgram();
        } catch (SyntaxError s) {
            System.exit(1);
        }

        Checker checkerInside = new Checker(newHandler);
        checkerInside.check(currentAST, fileName, false);

        Module referencedModule = modules.getModule(fileName);
        mainModule.addImportedFile(referencedModule, ast.ident.spelling);


        return null;
    }

    public Object visitAnyType(AnyType ast, Object o) {
        return null;
    }
}
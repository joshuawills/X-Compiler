package X.Checker;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Optional;

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

public class Checker implements Visitor {

    private boolean isStructLHS = false;

    private Type currentNumericalType = null;

    private final String[] errors = {
        "*0: main function is missing",
        "*1: return type of 'main' is not void",
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
        "*16: main function may only have one i32 and one optional i8** parameter following it",
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
        "*68: no such lib C function",
        "*69: incompatible type for cast expression",
        "*70: innapropriate use of pointer access",
        "*71: inappropriate use of 'null'",
        "*72: empty tuple type",
        "*73: only one type in tuple type",
        "*74: may only perform tuple access on tuples",
        "*75: tuple access index out of bounds",
        "*76: inappropriate tuple destructuring",
        "*77: identifier redeclared in tuple destructuring",
        "*78: no such lib C variable",
        "*79: imported standard libary does not exist. Is your 'X_LIB_PATH' set?",
        "*80: use of _ variable.",
        "*81: unknown method",
        "*82: no method found with provided parameter types",
        "*83: cannot pass immutable data type to mutable method",
        "*84: duplicate method declarations",
        "*85: function imported into namespace already exists",
        "*86: global var imported into namespace already exists",
        "*87: enum imported into namespace already exists",
        "*88: struct imported into namespace already exists",
        "*89: trait exists with same name",
        "*90: multiple methods in trait with the same name",
        "*91: empty trait",
        "*92: no such trait to implement",
        "*93: no such struct to implement trait for",
        "*94: unrecognised method for trait",
        "*95: missing methods for trait implementation",
        "*96: multiple method implementations for trait",
        "*97: type doesn't match with specified implementation",
        "*98: duplicate definitions of extern function or variable",
        "*99: duplicate generic types with same name",
        "*100: no such trait to specify as a generic bound",
        "*101: redeclaration of generic function",
        "*102: generic function imported into namespace already exists"
    };

    private final SymbolTable idTable;
    private final ErrorHandler handler;

    private boolean hasMain = false;
    private boolean hasReturn = false;
    private boolean inMain = false;
    private boolean declaringLocalVar = false;
    private boolean inCallExpr = false;

    // Used for checking break/continue stmt
    private int loopDepth = 0;

    // Used for assigning
    private int loopAssignDepth = 0;

    // Base statement counter, used for constructing variable names
    private int baseStatementCounter = 0;

    private boolean validDollar = false;
    private Type currentFunctionOrMethodType = null;

    private final Position dummyPos = new Position();

    public Checker(ErrorHandler handler) {
        this.handler = handler;
        this.idTable = new SymbolTable();
    }

    private AllModules modules = AllModules.getInstance();
    private Module mainModule;

    private String currentFileName;

    private String[] numPriority = {"U8", "I8", "U32", "I32", "U64", "I64", "F32", "F64"}; // Later ones take priority

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
       
        // Load in function names, extern functions, method names, 
        // generic functions and global vars
        loadFunctionsAndGlobalVars(L);

        // Loading in implementations
        loadImplementations(L);

        // Loading in generic functions
        loadGenerics(L);
       
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
            if (L.D.isImportStmt() || L.D.isUsingStmt()) {
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

            switch (L.D) {
                case Enum E -> {
                    if (E.isEmpty()) {
                        handler.reportError(errors[49] + ": %", E.I.spelling, E.I.pos);
                    }

                    // Duplicate enum keys
                    ArrayList<String> duplicates = E.findDuplicates();
                    if (!duplicates.isEmpty()) {
                        String message = "found keys '" + String.join(", ", duplicates)
                                + "' in enum '" + E.I.spelling + "'";
                        handler.reportError(errors[42] + ": %", message, E.I.pos);
                    }

                    // Duplicate type definitions
                    String message = "enum '" + E.I.spelling + "' clashes with previously declared ";
                    if (mainModule.enumExists(E.I.spelling)) {
                        String f = mainModule.enumExistsInUsing(E.I.spelling);
                        if (!f.equals("")) {
                            message = "enum in module '" + f + "'";
                            handler.reportError(errors[87] + ": %", message, E.pos);

                        } else {
                            message += "enum";
                            handler.reportError(errors[47] + ": %", message, E.I.pos);
                        }
                    } else if (mainModule.structExists(E.I.spelling)) {
                        message += "struct";
                        handler.reportError(errors[47] + ": %", message, E.I.pos);
                    }

                    mainModule.addEnum(E, currentFileName);
                }
                case Struct S -> {
                    S.fileName = currentFileName;
                    if (S.isEmpty()) {
                        handler.reportError(errors[48] + ": %", S.I.spelling, S.I.pos);
                    }

                    ArrayList<String> duplicates = S.findDuplicates();
                    if (!duplicates.isEmpty()) {
                        String message = "found members '" + String.join(", ", duplicates)
                                + "' in struct '" + S.I.spelling + "'";
                        handler.reportError(errors[45] + ": %", message, S.I.pos);
                    }

                    // Duplicate type definitions
                    String message = "struct '" + S.I.spelling + "' clashes with previously declared ";
                    if (mainModule.enumExists(S.I.spelling)) {
                        message += "enum";
                        handler.reportError(errors[47] + ": %", message, S.I.pos);
                    } else if (mainModule.structExists(S.I.spelling)) {
                        String f = mainModule.structExistsWithUsing(S.I.spelling);
                        if (!f.equals("")) {
                            message += "struct in module'" + f + "'";
                            handler.reportError(errors[88] + ": %", message, S.pos);

                        } else {
                            message += "struct";
                            handler.reportError(errors[47] + ": %", message, S.I.pos);
                        }
                    }

                    // header type is validated here
                    // potential subtypes that are user-created types will be validated when struct's initialised
                    mainModule.addStruct(S, currentFileName);
                }
                default -> {}
            }

            if (L.DL instanceof EmptyDeclList) {
                break;
            }
            L = (DeclList) L.DL;
        }
    }

    private void loadFunctionsAndGlobalVars(DeclList L) {
        while (true) {

            switch (L.D) {
                case GlobalVar G -> {
                    G.index = "";
                    visitVarDecl(G, G.T, G.I, G.E);
                }
                case Struct S -> {
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
                }
                case Trait T -> {
                    T.filename = currentFileName;
                    if (modules.traitExists(T.I.spelling)) {
                        Trait T2 = modules.getTrait(T.I.spelling);
                        Position p = T2.pos;
                        String f = T2.filename;
                        String message = T.I.spelling + ". Also found at " + p + "in " + f;
                        handler.reportError(errors[89] + ": %", message, T.I.pos);
                    } else {
                        modules.addTrait(T);
                        // Make sure the types are valid for all the functions
                        T.TL.visit(this, null);

                        if (T.TL.isEmptyTraitList()) {
                            handler.reportMinorError(errors[91] + ": %", T.I.spelling, T.pos);
                        }

                        // ensure there are no duplicates        
                        ArrayList<String> duplicates = T.findDuplicates();
                        if (!duplicates.isEmpty()) {
                            String message = String.join(", ", duplicates) + " in trait " + T.I.spelling;
                            handler.reportError(errors[90] + ": %", message, T.pos);
                        }

                    }

                }
                case Method M -> {
                    List P = M.PL;

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

                    checkMurking(M.attachedStruct);
                    checkMurking(M);

                    M.setTypeDef();
                    M.filename = currentFileName;
                    if (modules.methodExists(M.I.spelling, M.attachedStruct.T, M.PL)) {
                        handler.reportError(errors[84] + ": %", M.I.spelling, M.I.pos);
                    }
                    modules.addMethod(M);
                }
                case Function F -> {
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

                    checkMurking(F);

                    if (mainModule.functionExists(F.I.spelling, F.PL)) {
                        String f = mainModule.functionExistsInUsing(F.I.spelling, F.PL);
                        if (!f.isEmpty()) {
                            String message = "function '" + F.I.spelling + "' from module '" + f + "'";
                            handler.reportError(errors[85] + ": %", message, F.I.pos);
                        } else {
                            String message = String.format("'%s'. Previously declared at line %d", F.I.spelling,
                                    F.pos.lineStart);
                            handler.reportError(errors[2] + ": %", message, F.I.pos);
                        }
                    }

                    F.setTypeDef();
                    mainModule.addFunction(F, currentFileName);
                }
                case Extern E -> {
                    if (E.F != null) {
                        if (modules.libCFunctionExists(E.F.I.spelling)) {
                            handler.reportError(errors[98] + ": %", E.F.I.spelling, E.F.I.pos);
                        }
                        modules.addLibCFunction(E.F);
                    } else {
                        if (modules.libCVariableExists(E.G.I.spelling)) {
                            handler.reportError(errors[98] + ": %", E.G.I.spelling, E.G.I.pos);
                        }
                        modules.addLibCVariable(E.G);
                    }
                }
                default -> {}
            }

            if (L.DL.isEmptyDeclList()) {
                break;
            }
            L = (DeclList) L.DL;
        }
    }

    private void loadImplementations(DeclList D) {
        while (true) {
            switch (D.D) {
                case Impl I -> I.visit(this, null);
                default -> {}
            }
            if (D.DL.isEmptyDeclList()) {
                return;
            }
            D = (DeclList) D.DL;
        }
    }

    private ArrayList<String> currentGenericTypes = new ArrayList<>();
    private GenericFunction currentGenericFunction = null;

    private void loadGenerics(DeclList D) {
        while (true) {
            switch (D.D) {
                case GenericFunction G -> {
                    // Visit the generic types list and make sure all the traits exist
                    currentGenericFunction = G;
                    G.GTL.visit(this, null);

                    // Recalculating params for abstract types
                    List P = G.PL;
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

                    checkMurking(G);

                    // Checking if the generic function already exists
                    if (mainModule.genericFunctionExists(G.I.spelling, G.PL)) {
                        String f = mainModule.genericFunctionExistsInUsing(G.I.spelling, G.PL);
                        if (!f.isEmpty()) {
                            String message = "generic function '" + G.I.spelling + "' from module '" + f + "'";
                            handler.reportError(errors[102] + ": %", message, G.I.pos);
                        } else {
                            String message = String.format("'%s'. Previously declared at line %d", G.I.spelling,
                                    G.pos.lineStart);
                            handler.reportError(errors[101] + ": %", message, G.I.pos);
                        }
                    }

                    mainModule.addGenericFunction(G, currentFileName);
                    currentGenericTypes.clear();
                }
                default -> {}
            }
            if (D.DL.isEmptyDeclList()) {
                return;
            }
            D = (DeclList) D.DL;
        }
    }

    private void checkUnusedEntities() {
        Module M = AllModules.getInstance().getMainModule();

        for (Enum e: M.getEnums().values()) {
            if (!e.isUsed) {
                M.thisHandler.reportMinorError(errors[39] + ": %", e.I.spelling, e.I.pos);
            }
        }

        // Don't print for imported modules
        for (GlobalVar v: M.getVars().values()) {
            if (!v.isUsed && !v.I.spelling.equals("_")) {
                M.thisHandler.reportMinorError(errors[21] + ": %", v.I.spelling, v.I.pos);
            }
            if (v.isMut && !v.isReassigned) {
                M.thisHandler.reportMinorError(errors[22] + ": %", v.I.spelling, v.I.pos);
            }
        }

        // Don't print for imported modules
        for (Function f: M.getFunctions()) {
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

    private void establishEnv() {
        Environment.booleanType = new BooleanType(dummyPos);
        Environment.i8Type= new I8Type(dummyPos);
        Environment.i64Type = new I64Type(dummyPos);
        Environment.i32Type = new I32Type(dummyPos);
        Environment.u8Type = new U8Type(dummyPos);
        Environment.u32Type = new U32Type(dummyPos);
        Environment.u64Type = new U64Type(dummyPos);
        Environment.f32Type = new F32Type(dummyPos);
        Environment.f64Type = new F64Type(dummyPos);
        Environment.voidType = new VoidType(dummyPos);
        Environment.variaticType = new VariaticType(dummyPos);
        Environment.voidPointerType = new PointerType(dummyPos, Environment.voidType);
        Environment.errorType = new ErrorType(dummyPos);
        Environment.charPointerType = new PointerType(dummyPos, Environment.i8Type);
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

    public Object visitMethod(Method ast, Object o) {
        baseStatementCounter = 0;

        if (ast.isTraitFunction) {
            ast.PL.visit(this, o);
            checkMurking(ast);
            return null;
        }

        checkMurking(ast.attachedStruct);
        checkMurking(ast);
        this.currentFunctionOrMethodType = ast.T;

        if (ast.I.spelling.equals("$")) {
            handler.reportError(errors[24] + ": %", "can't be used as method name", ast.I.pos);
        }

        idTable.openScope();
        ast.attachedStruct.visit(this, o);
        ast.PL.visit(this, null);
        ast.S.visit(this, ast);
        idTable.closeScope();

        if (!hasReturn && !ast.T.isVoid()) {
            handler.reportError(errors[19], "", ast.I.pos);
        }

        this.currentFunctionOrMethodType = null;
        hasReturn = false;
        baseStatementCounter = 0;
        return null;
    }

    public Object visitFunction(Function ast, Object o) {
        baseStatementCounter = 0;

        // Check if func already exists with that name
        checkMurking(ast);
        this.currentFunctionOrMethodType = ast.T;
        if (ast.I.spelling.equals("main")) {
            inMain = hasMain = true;
            if (!ast.T.isVoid()) {
                String message = "set to " + ast.T.toString();
                handler.reportError(errors[1] + ": %", message, ast.I.pos);
            }
            if (ast.PL.isParaList() && !((ParaList) ast.PL).validMainParameters()) {
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

        this.currentFunctionOrMethodType = null;
        inMain = hasReturn = false;
        baseStatementCounter = 0;
        return ast.T;
    }

    public Object visitGlobalVar(GlobalVar ast, Object o) {
        return null;
    }

    private List getCurrentImplementsList(GenericFunction G, String s) {
        GenericTypeList GTL = (GenericTypeList) G.GTL;
        while (true) {
            if (GTL.I.spelling.equals(s)) {
                return GTL.IL;
            }
            if (GTL.GTL.isEmptyGenericTypeList()) {
                break;
            }
            GTL = (GenericTypeList) GTL.GTL;
        }
        return null;
    }

    private Type unMurk(MurkyType type) {
        String s = type.V.spelling;
        Module M = mainModule;

        // Handle generic functions
        if (currentGenericTypes.contains(s)) {
            List IL = getCurrentImplementsList(currentGenericFunction, s);
            return new GenericType(type.V, IL, type.pos);
        }

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
        } else if (ast.T.isMurkyArray()) {
            unMurkArr(ast);
        } else if (ast.T.isMurkyPointer()) {
            unMurkPointer(ast);
        } else if (ast.T.isTuple()) {
            
            // Loop over the types
            TypeList TL = (TypeList) ((TupleType) ast.T).TL;
            while (true) {
                if (TL.T.isMurky()) {
                    TL.T = unMurk((MurkyType) TL.T);
                } else if (TL.T.isMurkyArray()) {
                    ((ArrayType) TL.T).t = unMurk((MurkyType) ((ArrayType) TL.T).t);
                } else if (TL.T.isMurkyPointer()) {
                    ((PointerType) TL.T).t = unMurk((MurkyType) ((PointerType) TL.T).t);
                }
                if (TL.T.isTuple()) {
                    ((TupleType) TL.T).inAnotherTupleType = true;
                }
                if (TL.TL.isEmptyTypeList()) {
                    break;
                }

                TL = (TypeList) TL.TL;
            }

        }
    }

    private Object visitVarDecl(Decl ast, Type existingType, Ident I, Expr E) {
        if (ast.T.isTuple()) {
            ast.T.visit(this, null);
        } else {
            checkMurking(ast);
        }
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
            switch (ast) {
                case LocalVar L -> L.E = E;
                case GlobalVar G -> G.E = E;
                default -> {}
            }
            returnType = E.type;
        } else {
            returnType = (Type) E.visit(this, ast);
        }
        declaringLocalVar = false;
        currentNumericalType = null;

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
        switch (ast) {
            case LocalVar L -> L.E = e2AST;
            case GlobalVar G -> G.E = e2AST;
            default -> {}
        }
        return existingType;
    }

    private Expr checkCast(Type expectedT, Expr expr, AST parent) {

        if (expectedT.isVariatic() && expr.type.isF32()) {
            CastExpr E = new CastExpr(expr, expr.type, new F64Type(expr.type.pos), expr.pos, parent);
            E.visit(this, null);
            return E;
        } else if (expectedT.isVariatic()) {
            return expr;
        }

        if (expr.isNullExpr()) {
            return expr;
        }

        if (expectedT.isVoidPointer()) {
            if (!expr.type.isVoidPointer()) {
                CastExpr E = new CastExpr(expr, expr.type, expectedT, expr.pos, parent);
                E.visit(this, null);
                return E;
            }
        }

        if (expr.type != null && expr.type.isVoidPointer()) {
            if (expectedT.isVoidPointer()) {
                return expr;
            }
            // Assume expectedT is pointer
            CastExpr E = new CastExpr(expr, expr.type, expectedT, expr.pos, parent);
            E.visit(this, null);
            return E;
        }

        if (expectedT.isVoidPointer()) {
            return expr;
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

        Type t = expr.type;
        if (t == null || expectedT.assignable(t) || t.isError() || expectedT.equals(t)) {
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
                if (!V.isUsed && !V.I.spelling.equals("_")) {
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
            ast.E = (Expr) ast.E.visit(this, ast);
            ast.E.parent = ast;
            condT = ast.E.type;
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
        Type t;
        if (ast.E.isStructAccess() || ast.E.isIntOrDecimalExpr()) {
            ast.E = (Expr) ast.E.visit(this, ast);
            t= ast.E.type;
        } else {
            t= (Type) ast.E.visit(this, ast);
        }
        if (!t.isBoolean()) {
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
        switch (ast.parent) {
            case Stmt S -> S.containsExit = true;
            case List S -> S.containsExit = true;
            default -> {}
        }
        return null;
    }

    public Object visitContinueStmt(ContinueStmt ast, Object o) {
        if (this.loopDepth <= 0) {
            handler.reportError(errors[15], "", ast.pos);
        }
        ast.containsExit = true;
        switch (ast.parent) {
            case Stmt S -> S.containsExit = true;
            case List S -> S.containsExit = true;
            default -> {}
        }
        return null;
    }

    public Object visitReturnStmt(ReturnStmt ast, Object o) {
        this.hasReturn = true;
        boolean seenIncompatible = false;

        // Returning nothing but there's something to return
        if (ast.E.isEmptyExpr() && !(this.currentFunctionOrMethodType.isVoid())) {
            seenIncompatible = true;
            String message = "expected " + this.currentFunctionOrMethodType.toString() + ", received void";
            handler.reportError(errors[6] + ": %", message, ast.E.pos);
        }
        Type conditionType;

        if (ast.E.isEmptyExpr()) {
            conditionType = new VoidType(new Position());
            ast.E.type = Environment.voidType;
        } else {
            if (this.currentFunctionOrMethodType.isNumeric()) {
                currentNumericalType = this.currentFunctionOrMethodType;
            }
            if (ast.E.isDotExpr() || ast.E.isIntOrDecimalExpr()) {
                ast.E = (Expr) ast.E.visit(this, ast);
                conditionType= ast.E.type;
            } else {
                conditionType= (Type) ast.E.visit(this, ast);
            }
            currentNumericalType = null;
        }
        if (!this.currentFunctionOrMethodType.assignable(conditionType) && !seenIncompatible) {
            String message = "expected " + this.currentFunctionOrMethodType.toString() +
                ", received " + conditionType.toString();
            handler.reportError(errors[6] + ": %", message, ast.E.pos);
        }

        ast.containsExit = true;
        switch (ast.parent) {
            case Stmt S -> S.containsExit = true;
            case List S -> S.containsExit = true;
            default -> {}
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
        return T1.equals(T2);
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
        } else if (T.isPointer()) {
            return "p";
        } else if (T.isU8()) {
            return "u8";
        } else if (T.isU32()) {
            return "u32";
        } else if (T.isU64()) {
            return "u64";
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

        if (t1.isVoidPointer() || t2.isVoidPointer()) {
            if (!(ast.O.spelling.equals("==") || ast.O.spelling.equals("!="))) {
                handler.reportError(errors[70], "", ast.pos);
            }
            ast.O.spelling = "p" + ast.O.spelling;
            ast.type = Environment.booleanType;
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
                }
                else if (t1.isPointer() && t2.isPointer()) {
                }
                else if (!v1Numeric || !v2Numeric) {
                    handler.reportError(errors[7], "", t2.pos);
                    ast.type = Environment.errorType;
                    break;
                }
                ast.type = Environment.booleanType;
            }
            case "<", "<=", ">", ">=" -> {
                if (t1.isPointer() && t2.isPointer()) {
                }
                else if (!v1Numeric || !v2Numeric) {
                    handler.reportError(errors[7], "", ast.pos);
                    ast.type = Environment.errorType;
                    break;
                }
               ast.type = Environment.booleanType;
            }
            case "+", "-", "/", "*", "%" -> {
                boolean validV1 = v1Numeric || t1.isPointer();
                boolean validV2 = v2Numeric || t2.isPointer();

                if (!validV1|| !validV2) {
                    handler.reportError(errors[7], "", ast.pos);
                    ast.type = Environment.errorType;
                    break;
                }

                if (t1.isPointer() && t2.isPointer()) {
                    handler.reportError(errors[7], "", ast.pos);
                    ast.type = Environment.errorType;
                    break;
                }

                if (t1.isPointer() && t2.isNumeric()) {
                    // Can only be "+" or "-"
                    if (!ast.O.spelling.equals("+") && !ast.O.spelling.equals("-")) {
                        handler.reportError(errors[7], "", ast.pos);
                        ast.type = Environment.errorType;
                        break;
                    }
                    ast.type = t1;
                    break;
                } else if (t2.isPointer()) {
                    handler.reportError(errors[7], "", ast.pos);
                    ast.type = Environment.errorType;
                    break;
                }

                ast.type = t1;
            }
            default -> {
                // Not sure why it goes in here sometimes
                return ast.type;
            }
        }

        if (ast.type.isBoolean()) {
            if (isSameType(t1, t2)) {
                ast.O.spelling = getPrefix(t1) + ast.O.spelling;
                return ast.type;
            }

            // Not including enum types!
            assert((t1.isNumeric() || t1.isPointer()) && (t2.isNumeric() || t2.isPointer()));
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

        assert((t1.isNumeric() || t1.isPointer()) && (t2.isNumeric() || t2.isPointer()));

        if (t1.isPointer()) {
            // t2 is not pointer
            ast.O.spelling = "p" + ast.O.spelling;
        } else if (prioritiseIntTypes(t1, t2)) {
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
        if (!ast.D.isImpl()) {
            ast.D.visit(this, null);
        }
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

        if (ast.SL.containsExit) {
            ast.containsExit = true;
        }
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

    public Object visitU8Expr(U8Expr ast, Object o) {
        ast.type = Environment.u8Type;
        return ast.type;
    }

    public Object visitU32Expr(U32Expr ast, Object o) {
        ast.type = Environment.u32Type;
        return ast.type;
    }

    public Object visitU64Expr(U64Expr ast, Object o) {
        ast.type = Environment.u64Type;
        return ast.type;
    }

    public Object visitIntExpr(IntExpr ast, Object o) {
        
        Expr E = null;
        if (currentNumericalType == null || currentNumericalType.isI64() || currentNumericalType.isFloat()) {
            E = new I64Expr(ast.IL, ast.pos);
        } else if (currentNumericalType.isI32()) {
            E = new I32Expr(ast.IL, ast.pos);
        } else if (currentNumericalType.isI8()) {
            E = new I8Expr(ast.IL, ast.pos);
        } else if (currentNumericalType.isU8()) {
            E = new U8Expr(ast.IL, ast.pos);
        } else if (currentNumericalType.isU32()) {
            E = new U32Expr(ast.IL, ast.pos);
        } else if (currentNumericalType.isU64()) {
            E = new U64Expr(ast.IL, ast.pos);
        }
         
        if (!E.isIntExpr()) {
            E.visit(this, o);
            E.parent = ast.parent;
        } else {
            E.type = Environment.i64Type;
        }
        E.type.parent = ast.parent;

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

    public Object visitU8Type(U8Type ast, Object o) {
        return Environment.u8Type;
    }

    public Object visitU32Type(U32Type ast, Object o) {
        return Environment.u32Type;
    }

    public Object visitU64Type(U64Type ast, Object o) {
        return Environment.u64Type;
    }

    public Object visitVariaticType(VariaticType ast, Object o) {
        return Environment.variaticType;
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
            if (decl.isGlobalVar() || decl.isParaDecl()) {
                handler.reportError(errors[2] +": %", message, ident.pos);
            } else {
                if (!ident.spelling.equals("_") && !ident.spelling.equals("err")) {
                    handler.reportMinorError(errors[2] +": %", message, ident.pos);
                }
                idTable.remove(entry);
            }
       }

        if (decl.isGlobalVar()) {
            if (mainModule.varExists(ident.spelling)) {
                String f = mainModule.varExistsInUsing(ident.spelling);
                if (!f.equals("")) {
                    GlobalVar G1 = mainModule.getVar(ident.spelling, true);
                    mainModule.thisHandler.reportError(errors[86] + ": %", "variable '" + ident.spelling  + "' from module '" + f + "'. Existing declaration below.", G1.I.pos);
                } else {
                    handler.reportError(errors[2] + ": %", ident.spelling, ident.pos);
                }
            }
            mainModule.addGlobalVar((GlobalVar) decl, currentFileName);
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

        if (ast.I.spelling.equals("_")) {
            handler.reportMinorError(errors[80], "", ast.I.pos);
        }

        if (declaringLocalVar) {
            ast.setDeclaringLocalVar();
        }

        if (inCallExpr) {
            ast.setInCallExpr();
        }

        if (ast.isLibC) {
            if (!modules.libCVariableExists(ast.I.spelling)) {
                handler.reportError(errors[78] + ": %", ast.I.spelling, ast.I.pos);
                return Environment.errorType;
            }
            GlobalVar G = modules.getLibCVariable(ast.I.spelling);
            ast.I.decl = G;
            return G.T;
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
        boolean isLocalVar = ast.parent.parent.isLocalVar();
        boolean isAssignmentExpr = ast.parent.parent.isAssignmentExpr();
        if (decl.T.isArray() && !isFnCall && !isStructDecl && !isLocalVar && !isAssignmentExpr) {
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

        if (binding.isFunction() || !(binding.T.isArray() || binding.T.isPointer())) {
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
        currentNumericalType = Environment.i64Type;
        if (ast.index.isDotExpr() || ast.index.isIntOrDecimalExpr()) {
            ast.index = (Expr) ast.index.visit(this, o);
            T = ast.index.type;
        } else {
            T = (Type) ast.index.visit(this, o);
        }
        currentNumericalType = null;

        if (!T.isSignedInteger() && !T.isUnsignedInteger()) {
            handler.reportError(errors[37] + ": %", ast.I.spelling, ast.I.pos);
            return Environment.errorType;
        }

        if (!T.isI64()) {
            ast.index = new CastExpr(ast.index, T, Environment.i64Type, ast.index.pos, ast);
        }

        ast.parentType = binding.T;
        if (binding.T.isArray()) {
            ast.type = ((ArrayType) binding.T).t;
        } else {
            ast.type = ((PointerType) binding.T).t;
        }
        return ast.type;
    }

    public Object visitI8Type(I8Type ast, Object o) {
        return Environment.i8Type;
    }

    public Object visitCharLiteral(CharLiteral ast, Object o) {
        return Environment.i8Type;
    }

    public Object visitCharExpr(I8Expr ast, Object o) {
        if (ast.CL.isPresent()) {
            int l = ast.CL.get().spelling.length();
            if (l != 1) {
                String m = "received '" + ast.CL.get().spelling + "'";
                    handler.reportMinorError(errors[38] + ": %", m, ast.CL.get().pos);
                return Environment.errorType;
            }
        }

        ast.type = Environment.i8Type;
        return ast.type;
    }

    public Object visitCallExpr(CallExpr ast, Object o) {
        inCallExpr = true;
        if (inMain && ast.I.spelling.equals("main")) {
            handler.reportError(errors[17], "", ast.I.pos);
            return Environment.errorType;
        }

        if (ast.TypeDef == null) {
            String TL = genTypes(ast.AL, o);
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

                if (!specificModule.functionExists(ast.I.spelling, ast.AL)) {
                    if (specificModule.functionWithNameExists(ast.I.spelling)) {
                        handler.reportError(errors[43] + ": %", message, ast.I.pos);
                    } else {
                        message = "function '" + ast.I.spelling + "' in module '" + moduleAlias + "'";
                        handler.reportError(errors[65] + ": %", message, ast.I.pos);
                    }
                    ast.type = Environment.errorType;
                    return ast.type;
                } else {
                    Function function = specificModule.getFunction(ast.I.spelling, ast.AL);
                    if (!function.isExported) {
                        message = "function '" + ast.I.spelling + "' in module '" + moduleAlias + "'";
                        handler.reportError(errors[64] + ": %", message, ast.I.pos);
                        ast.type = Environment.errorType;
                        return ast.type;
                    }
                    ast.I.decl = function;
                    function.setUsed();
                    ast.AL.visit(this, function.PL);
                    ast.setTypeDef(function.TypeDef);
                    ast.type = function.T;
                    return function.T;
                }
            }
        }
        else if (!mainModule.functionExists(ast.I.spelling, ast.AL)) {
            if (idTable.retrieve(ast.I.spelling) != null || mainModule.varExists(ast.I.spelling)) {
                handler.reportError(errors[10] + ": %", ast.I.spelling, ast.I.pos);
            } else if (mainModule.functionWithNameExists(ast.I.spelling)) {
                handler.reportError(errors[43] + ": %", ast.I.spelling, ast.I.pos);
            } else if (mainModule.functionExistsNotExported(ast.I.spelling, ast.AL)) {
                handler.reportError(errors[64] + ": %", ast.I.spelling, ast.I.pos);
            } else {
                handler.reportError(errors[4] + ": %", ast.I.spelling, ast.I.pos);
            }
            ast.type = Environment.errorType;
            return ast.type;
        }

        Function function;
        if (ast.isLibC) {
            function = modules.getLibCFunction(ast.I.spelling);
        } else {
            function = mainModule.getFunction(ast.I.spelling, ast.AL);
        }
        ast.setTypeDef(function.TypeDef);
        ast.I.decl = function;
        function.setUsed();
        ast.AL.visit(this, function.PL);
        inCallExpr = false;
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
            ((Args) head).E.type = t;
            options.add(t.getMini());
            head = ((Args) head).EL;
        }
        return String.join("_", options);
    }


    public Object visitLocalVarStmt(LocalVarStmt ast, Object o) {
        ast.V.visit(this, o);
        return null;
    }

    public Object visitTupleDestructureAssignStmt(TupleDestructureAssignStmt ast, Object o) {
        ast.TDA.visit(this, o);
        return null;
    }

    public Object visitStringExpr(StringExpr ast, Object o) {
        String v = ast.SL.spelling;

        if (modules.stringConstantsMapping.containsKey(v)) {
            ast.index = modules.stringConstantsMapping.get(v);
        } else {
            modules.stringConstantsMapping.put(v, modules.strCount);
            ast.index = modules.strCount++;
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
            if (!elem.get().T.isArray() && !elem.get().T.isPointer()) {
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
                if (elem.get().T.isArray()) {
                    return ((ArrayType) elem.get().T).t;
                }
                return ((PointerType) elem.get().T).t;
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
        ast.E.parent = ast;
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
            || o instanceof DerefExpr || o instanceof DotExpr || o instanceof TupleAccess;
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

        if (ast.O.spelling.equals("/=") || ast.O.spelling.equals("*=")
            || ast.O.spelling.equals("-=")|| ast.O.spelling.equals("+=")) {
            String O = String.valueOf(ast.O.spelling.charAt(0));
            ast.RHS = new BinaryExpr(ast.LHS, ast.RHS, new Operator(O, ast.RHS.pos), ast.RHS.pos);
            ast.RHS.visit(this, o);
        }

        if (ast.RHS.isDotExpr() || ast.RHS.isIntOrDecimalExpr()) {
            ast.RHS = (Expr) ast.RHS.visit(this, o);
            realType = ast.RHS.type;
        } else {
            realType = (Type) ast.RHS.visit(this, o);
        }
        currentNumericalType = null;
        declaringLocalVar = false;

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

        ast.RHS = checkCast(expectedType, ast.RHS, ast);
        ast.type = expectedType;
        return ast.type;
    }

    public Object visitNullExpr(NullExpr ast, Object o) {
        if (ast.parent.isArgs() || ast.parent.isLocalVar() || ast.parent.isAssignmentExpr() || ast.parent.isBinaryExpr()) {
            ast.type = Environment.voidPointerType;
            return Environment.voidPointerType;
        }
        handler.reportError(errors[71], "", ast.pos);
        ast.type = Environment.errorType;
        return Environment.errorType;
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

    // Currently just assuming it's not gonna be a module access of a global variable TODO: fix that
    public Object visitMethodAccessExpr(MethodAccessExpr ast, Object o) {
        Expr errorExpr = new EmptyExpr(ast.pos);
        errorExpr.type = Environment.errorType;

        if (ast.TypeDef == null) {
            String TL = genTypes(ast.args, o);
            ast.TypeDef = TL;
        }

        Type T = currentTypeMethodAccess;
        if (!modules.methodExists(ast.I.spelling, T, ast.args)) {
            String message = " type " + T + ", with method name " + ast.I.spelling;
            if (!modules.methodWithNameExists(ast.I.spelling, T)) {
                handler.reportError(errors[81] + ": %", message, ast.pos);
            } else {
                handler.reportError(errors[82] + ": %", message, ast.pos);
            }
            return errorExpr;
        }
        
        Method M = modules.getMethod(ast.I.spelling, T, ast.args);

        if (M.attachedStruct.isMut && !isCurrentMethodMutable) {
            handler.reportError(errors[83] + ": %", "'" + ast.I.spelling + "'", ast.pos);
            return errorExpr;
        }
        isCurrentMethodMutable = true;

        M.setUsed();
        ast.args.visit(this, M.PL);
        ast.TypeDef = M.TypeDef;
        ast.type = M.T;
        ast.ref = M;
        currentTypeMethodAccess = ast.type;
        if (ast.next != null) {
            ast.next.visit(this, o);
        }
        return ast;
    }

    private Type currentTypeMethodAccess = null;
    private boolean isCurrentMethodMutable = false;

    // Currently operating under the presumption there's no pointer accesses
    public Object visitDotExpr(DotExpr ast, Object o) {

        Expr errorExpr = new EmptyExpr(ast.pos);
        errorExpr.type = Environment.errorType;

        if (ast.E.isMethodAccessExpr()) {
            if (ast.IE.isVarExpr()) {
                Ident I = ((SimpleVar) ((VarExpr) ast.IE).V).I;
                boolean isGlobalVar = mainModule.varExists(I.spelling);
                boolean isLocalVar = idTable.retrieve(I.spelling) != null;
                if (!(isLocalVar || isGlobalVar)) {
                    handler.reportError(errors[56], "", ast.pos);
                    return errorExpr;
                }
                Decl d;
                if (isLocalVar) {
                    d = idTable.retrieve(I.spelling);
                } else {
                    d = mainModule.getVar(I.spelling);
                }
                d.isUsed = true;
                I.decl = d;
                currentTypeMethodAccess = d.T;
                ((VarExpr) ast.IE).type = d.T;
                isCurrentMethodMutable = d.isMut;
            } else {
                if (ast.IE.isDotExpr() || ast.IE.isIntOrDecimalExpr()) {
                    ast.IE = (Expr) ast.IE.visit(this, o);
                } else {
                    ast.IE.visit(this, o);
                }
                currentTypeMethodAccess = ast.IE.type;
                isCurrentMethodMutable = true;
            }

            MethodAccessExpr E1;
            try {
                E1= (MethodAccessExpr) ast.E.visit(this, o);
            } catch (Exception e) {
                return errorExpr;
            } 
            E1.refExpr = ast.IE;
            MethodAccessWrapper MAW = new MethodAccessWrapper(E1);
            MAW.visit(this, o);
            currentTypeMethodAccess = null;
            return MAW;
        }

        Module M = mainModule;
        boolean isExternalModule = false;
        String message = "";
        
        if (!ast.IE.isVarExpr()) {
            handler.reportError(errors[56], "", ast.pos);
            return errorExpr;
        }

        Ident I = ((SimpleVar) ((VarExpr) ast.IE).V).I; 
        if (I.isModuleAccess) {
            String alias = I.module.get();
            if (!mainModule.aliasExists(alias)) {
                handler.reportError(errors[63] + ": %", "'" + alias + "'", I.pos);
                return errorExpr;
            }
            M = mainModule.getModuleFromAlias(alias);
            isExternalModule = true;
        }


        // First check if the identifier is an enum name
        if (M.enumExists(I.spelling)) {
            Enum E = M.getEnum(I.spelling);
            E.isUsed = true;

            DotExpr innerE = (DotExpr) ast.E;
            if (!innerE.E.isEmptyExpr()) {
                handler.reportError(errors[55], "", ast.pos);
                return Environment.errorType;
            }
            if (!innerE.IE.isVarExpr()) {
                handler.reportError(errors[56], "", ast.pos);
                return Environment.errorType;
            }   
            if (ast.arrayIndex.isPresent()) {
                handler.reportError(errors[32], "", ast.pos);
                return Environment.errorType;
            }
            if (ast.isPointerAccess) {
                handler.reportError(errors[70] + ": %", "cannot be used on enum expression",
                    ast.pos);
                return Environment.errorType;
            }
            Ident innerI = ((SimpleVar) ((VarExpr) innerE.IE).V).I;
            EnumExpr newEnum = new EnumExpr(I, innerI, ast.pos);
            newEnum.visit(this, o);
            return newEnum;
        } else {
            // Assumption is now we have an attempted struct access
            Decl d;
            if (!isExternalModule) {
                boolean isGlobalVar = M.varExists(I.spelling);
                boolean isLocalVar = idTable.retrieve(I.spelling) != null;
                if (!(isLocalVar || isGlobalVar)) {
                    handler.reportError(errors[56], "", ast.pos);
                    return errorExpr;
                }

                if (isLocalVar) {
                    d = idTable.retrieve(I.spelling);
                } else {
                    d = M.getVar(I.spelling);
                }
            } else {
                // Needs to be a global variable
                if (!M.varExists(I.spelling)) {
                    message = I.module.get() + "::" + I.spelling;
                    handler.reportError(errors[66] + ": %", message, I.pos);
                    return errorExpr;
                }
                GlobalVar G = M.getVar(I.spelling);
                if (!G.isExported) {
                    message = "variable '" + I.spelling + "' in module '" + I.module.get() + "'";
                    handler.reportError(errors[64] + ": %", message, I.pos);
                    return Environment.errorType;
                }
                d = G;
            }

            if ((!d.isMut && !ast.isPointerAccess) && isStructLHS) {
                handler.reportError(errors[20] + ": %", "'" + I.spelling + "'", ast.pos);
                return errorExpr;
            }

            Ident varName = d.I;
            Struct ref = null;

            if (ast.isPointerAccess) {
                if (!d.T.isPointer()) {
                    handler.reportError(errors[70] + ": %", "specified variable is not a pointer. Use '.' instead", ast.pos);
                    return errorExpr;
                }
            }

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

                if (!indexType.isSignedInteger() && !indexType.isUnsignedInteger()) {
                    handler.reportError(errors[37], "", ast.pos);
                    return errorExpr;
                }

                if (!innerT.isStruct()) {
                    handler.reportError(errors[57] + ": %", varName.spelling, ast.pos);
                    return errorExpr;
                }
                ref = ((StructType) innerT).S;
            }
            else if (!d.T.isStruct() && !(ast.isPointerAccess && d.T.isPointer() && ((PointerType) d.T).t.isStruct())) {
                handler.reportError(errors[57] + ": %", varName.spelling, ast.pos);
                return errorExpr;
            } else if (ast.isPointerAccess) {
                ref = ((StructType) ((PointerType) d.T).t).S;
            }
            else {
                ref = ((StructType) d.T).S;
            }
            StructAccessList SL = generateStructAccessList((DotExpr) ast.E);
            StructAccess SA = new StructAccess(ref, varName, SL, ast.pos, ast.arrayIndex, d.T, ast.isPointerAccess);
            SA.parent = ast.parent;
            SA.isLHSOfAssignment = ast.isLHSOfAssignment;
            SA.visit(this, o);
            return SA;
        }
    }

    public StructAccessList generateStructAccessList(DotExpr ast) {
        assert(ast.IE.isVarExpr());
        Ident I = ((SimpleVar) ((VarExpr) ast.IE).V).I;
        if (ast.E instanceof EmptyExpr) {
            return new StructAccessList(I, new EmptyStructAccessList(ast.pos), ast.pos, ast.arrayIndex, ast.isPointerAccess);
        }
        return new StructAccessList(I, generateStructAccessList((DotExpr) ast.E), ast.pos, ast.arrayIndex, ast.isPointerAccess);
    }


    public Object visitExprStmt(ExprStmt ast, Object o) {
        boolean isMethodAccess = ast.E.isDotExpr() && ((DotExpr) ast.E).E.isMethodAccessExpr();
        if (!(ast.E.isAssignmentExpr() || ast.E.isCallExpr() || isMethodAccess)) {
            System.out.println(ast.E);
            // if (ast.E.isCass)
            System.out.println(ast.E.pos);
            System.out.println(currentFileName);
            System.out.println("Should never be reached");
        }

        if (ast.E.isDotExpr()) {
            Expr E = (Expr) ast.E.visit(this, o);
            ast.E = E;
        } else {
            ast.E.visit(this, o);
        }
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
                ast.typeV.get().parent = ast;
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
            } else if (T.isMurkyArray()) {
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

        if (ast.manualCast) {
            Type realT;
            if (ast.E.isDotExpr() || ast.E.isIntOrDecimalExpr()) {
                ast.E = (Expr) ast.E.visit(this, o);
                realT = ast.E.type;
            } else {
                realT = (Type) ast.E.visit(this, o);
            }

            ast.type = ast.tTo;
            if (ast.tTo.isMurky()) {
                ast.tTo = unMurk((MurkyType) ast.tTo);
            } else if (ast.tTo.isMurkyPointer()) {
                MurkyType MT = (MurkyType) ((PointerType) ast.tTo).t;
                Type T = unMurk(MT);
                ((PointerType) ast.tTo).t = T;
            } else if (ast.tTo.isMurkyArray()) {
                MurkyType MT = (MurkyType) ((ArrayType) ast.tTo).t;
                Type T = unMurk(MT);
                ((ArrayType) ast.tTo).t = T;
            }

            ast.tFrom = realT;
            if (!realT.assignable(ast.tTo)) {
                String message = "expected " + ast.tTo + ", received " + realT;
                handler.reportError(errors[69] + ": %", message, ast.pos);
                return Environment.errorType;
            }
        }

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

    public Object visitUsingStmt(UsingStmt ast, Object o) {
        Path finalPath;
        if (!ast.isSTLImport) {
            Path basePath = Paths.get(currentFileName).resolve(Paths.get("..")).normalize();
            Path relativePath = Paths.get(ast.path.SL.spelling);
            finalPath = basePath.resolve(relativePath).normalize();
        } else {
            Path basePath = modules.libPath;
            Path relativePath = Paths.get(ast.ident.spelling + ".x");
            finalPath = basePath.resolve(relativePath).normalize();
        }
        String fileName = finalPath.toString();
        File file = new File(fileName);
        if (!file.exists() || (file.exists() && file.isDirectory())) {
            if (ast.isSTLImport) {
                handler.reportError(errors[79] + ": %", "'" + ast.ident.spelling + "'", ast.ident.pos);
            } else {
                handler.reportError(errors[61] + ": %", "'" + ast.path.SL.spelling + "'", ast.path.SL.pos);
            }
            return null;
        }

        if (mainModule.importedFileExists(fileName)) {
            handler.reportError(errors[62] + ": %", "'" + ast.path.SL.spelling + "'", ast.path.SL.pos);
            return null;
        }

        if (modules.moduleExists(fileName)) {
            // We've already checked this module, but we need to load it into the current module
            Module M = modules.getModule(fileName);
            mainModule.addUsingFile(M, fileName);
            return null;
        }

        // We need to actually analyse the module
        Module M = AnalyseModule(fileName);
        mainModule.addUsingFile(M, fileName);
        return null;
    }
    
    private Module AnalyseModule(String fileName) {
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
        return referencedModule;

    }

    private void CheckDuplicatesInModule(Module M, UsingStmt ast, String filename) {

        String moduleName;
        if (ast.path != null) {
            moduleName = ast.path.SL.spelling;
        } else {
            moduleName = ast.ident.spelling;
        }

    }

    public Object visitImportStmt(ImportStmt ast, Object o) {
        Path finalPath;
        if (!ast.isSTLImport) {
            Path basePath = Paths.get(currentFileName).resolve(Paths.get("..")).normalize();
            Path relativePath = Paths.get(ast.path.SL.spelling);
            finalPath = basePath.resolve(relativePath).normalize();
        } else {
            Path basePath = modules.libPath;
            Path relativePath = Paths.get(ast.ident.spelling + ".x");
            finalPath = basePath.resolve(relativePath).normalize();
        }
        String fileName = finalPath.toString();
        File file = new File(fileName);
        if (!file.exists() || (file.exists() && file.isDirectory())) {
            if (ast.isSTLImport) {
                handler.reportError(errors[79] + ": %", "'" + ast.ident.spelling + "'", ast.ident.pos);
            } else {
                handler.reportError(errors[61] + ": %", "'" + ast.path.SL.spelling + "'", ast.path.SL.pos);
            }
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

        Module referencedModule = AnalyseModule(fileName);
        mainModule.addImportedFile(referencedModule, ast.ident.spelling);
        return null;
    }

    public Object visitTypeList(TypeList ast, Object o) {

        if (ast.T.isMurky()) {
            ast.T = unMurk((MurkyType) ast.T);
        } else if (ast.T.isMurkyPointer()) {
            MurkyType MT = (MurkyType) ((PointerType) ast.T).t;
            Type T = unMurk(MT);
            ((PointerType) ast.T).t = T;
        } else if (ast.T.isMurkyArray()) {
            MurkyType MT = (MurkyType) ((ArrayType) ast.T).t;
            Type T = unMurk(MT);
            ((ArrayType) ast.T).t = T;
        }
        if (ast.T.isTuple()) {
            ((TupleType) ast.T).inAnotherTupleType = true;
        }
        ast.T.parent = ast;
        ast.TL.visit(this, o);
        return null;

    }

    public Object visitEmptyTypeList(EmptyTypeList ast, Object o) {
        return null;
    }

    public Object visitTupleType(TupleType ast, Object o) {
        if (!ast.cachedMurky) {
            if (ast.TL.isEmptyTypeList()) {
                handler.reportError(errors[72], "", ast.pos);
            } else {
                List subList = ((TypeList) ast.TL).TL;
                if (subList.isEmptyTypeList()) {
                    handler.reportError(errors[73], "", ast.pos);
                }
            }

            ast.TL.visit(this, o);
            ast.cachedMurky = true;

            if (!modules.tupleTypeExists(ast)) {
                modules.addTupleType(ast);
            }
            ast.index = modules.getTupleTypeIndex(ast);
        }
        return null;
    }

    public Object visitTupleExpr(TupleExpr ast, Object o) {
        TypeList TL = (TypeList) ast.EL.visit(this, o);
        Type T = new TupleType(TL, ast.pos);
        T.visit(this, o);
        ast.type = T;
        T.parent = ast;
        return T;
    }

    public Object visitTupleExprList(TupleExprList ast, Object o) {
        if (ast.E.isDotExpr() || ast.E.isIntOrDecimalExpr()) {
            ast.E = (Expr) ast.E.visit(this, o);
        } else {
            ast.E.visit(this, o);
        }
        Type T = ast.E.type;
        return new TypeList(T, (List) ast.EL.visit(this, o), ast.pos);
    }

    public Object visitEmptyTupleExprList(EmptyTupleExprList ast, Object o) {
        return new EmptyTypeList(ast.pos);
    }

    public Object visitTupleAccess(TupleAccess ast, Object o) {
        
        Decl d = idTable.retrieve(ast.I.spelling);

        if (d == null) {
            handler.reportError(errors[4] + ": %", ast.I.spelling, ast.I.pos);
            return Environment.errorType;
        }

        if (!d.T.isTuple()) {
            handler.reportError(errors[74] + ": %", ast.I.spelling, ast.I.pos);
            return Environment.errorType;
        }

        d.isUsed = true;
        if (ast.isLHSOfAssignment) {
            if (!d.isMut) {
                handler.reportError(errors[20] + ": %", ast.I.spelling, ast.I.pos);
                return Environment.errorType;
            }
            d.isReassigned = true;
        }

        ast.I.decl = d;
        SimpleVar VS = new SimpleVar(ast.I, ast.I.pos);
        ast.V = VS;

        TupleType T = (TupleType) d.T;
        ast.ref = T;
        int tupleLen = T.getLength();
        int providedLen = Integer.parseInt(ast.index.IL.spelling);

        if (providedLen > (tupleLen - 1)) {
            handler.reportError(errors[75] + ": %", ast.I.spelling, ast.I.pos);
            return Environment.errorType;
        }

        Type innerT = T.getNthType(providedLen);
        ast.type = innerT;
        return innerT;
    }

    private boolean isCurrentTupleDestructureMut = false;

    public Object visitTupleDestructureAssign(TupleDestructureAssign ast, Object o) {

        // Checking the provided type
        if (ast.T.isTuple()) {
            ast.T.visit(this, o);
        } else if (!ast.T.isUnknown()) {
            checkMurking(ast);
            if (!ast.T.isTuple()) {
                String message = "expression is type: " + ast.T + ", not a tuple";
                handler.reportError(errors[76] + ": %", message, ast.E.pos);
                return Environment.errorType;
            }
        }
        
        // Visiting the expression 
        if (ast.E.isDotExpr() || ast.E.isIntOrDecimalExpr()) {
            ast.E = (Expr) ast.E.visit(this, o);
        } else {
            ast.E.visit(this, o);
        }

        // Ensuring the provided expression is a tuple
        Type realType = ast.E.type;
        if (!realType.isTuple()) {
            String message = "expression is type: " + realType + ", not a tuple";
            handler.reportError(errors[76] + ": %", message, ast.E.pos);
            return Environment.errorType;
        }

        // Now we know both types are tuples at least 
        TupleType realTupleType = (TupleType) realType;
        if (!ast.T.isUnknown()) {
            if (!realTupleType.assignable(ast.T)) {
                String message = "expected " + ast.T + ", received " + realTupleType;
                handler.reportError(errors[5] + ": %", message, ast.E.pos);
                return Environment.errorType;
            }
        } else {
            // Need to make sure we have the right number of elements
            int realLength = realTupleType.getLength();
            int providedLength = ast.getLength();
            if (realLength != providedLength) {
                String message = "expected " + realLength + " elements, attempted to destructure" + providedLength + " elements.";
                handler.reportError(errors[76] + ": %", message, ast.E.pos);
                return Environment.errorType;
            }
        }
        
        if (ast.T.isUnknown()) {
            ast.T = realTupleType;
        }

        isCurrentTupleDestructureMut = ast.isMut;
        ast.idents.visit(this, realTupleType.TL);
        return null;
    }

    public Object visitIdentsList(IdentsList ast, Object o) {
        TypeList TL = (TypeList) o;
        String s = ast.I.spelling;

        LocalVar L = new LocalVar(TL.T, ast.I, new EmptyExpr(dummyPos), ast.pos, isCurrentTupleDestructureMut);
        L.index = String.format("%d_%d", loopAssignDepth, baseStatementCounter);
        declareVariable(ast.I, L);

        ast.indexT = String.format("%d_%d", loopAssignDepth, baseStatementCounter);
        baseStatementCounter += 1;

        ast.thisT = TL.T;
        ast.I.visit(this, o);
        ast.IL.visit(this, TL.TL);
        return null;
    }

    public Object visitEmptyIdentsList(EmptyIdentsList ast, Object o) {
        return null;
    }

    public Object visitMethodAccessWrapper(MethodAccessWrapper ast, Object o) {
        // Need to traverse down to find the deepest method access, and bind the type
        MethodAccessExpr E = ast.methodAccessExpr;
        while (true) {
            if (E.next.isEmptyExpr()) {
                break;
            }
            E = (MethodAccessExpr) E.next;
        }
        ast.type = E.type;
        return ast.type;
    }

    public Object visitTrait(Trait ast, Object o) {
        return null;
    }

    public Object visitEmptyTraitList(EmptyTraitList ast, Object o) {
        return null;
    }

    public Object visitTraitList(TraitList ast, Object o) {
        ast.TF.visit(this, o);
        ast.L.visit(this, o);
        return null;
    }

    private Impl currentImpl = null;

    public Object visitImpl(Impl ast, Object o) {
        
        // Validating the trait exists
        if (!modules.traitExists(ast.trait.spelling)) {
            String m = ast.trait.spelling;
            handler.reportError(errors[92] + ": %", m, ast.trait.pos);
            return null;
        }
        Trait T = modules.getTrait(ast.trait.spelling);
        ast.setTrait(T);

        // Validating the struct exists
        if (!mainModule.structExists(ast.struct.spelling)) {
            String m = "struct '" + ast.struct.spelling + "'";
            handler.reportError(errors[93] + ": %", m, ast.struct.pos);
            return null;
        }
        Struct S = mainModule.getStruct(ast.struct.spelling);
        ast.setStruct(S);
        modules.addTraitToStruct(S, T);

        currentImpl = ast;
        ast.IL.visit(this, o);
        currentImpl = null;

        ArrayList<Method> unimplementedMethods = ast.getUnimplementedMethods();
        if (!unimplementedMethods.isEmpty()) {
            String message = "trait '" + ast.trait.spelling + "' requires the following methods to be implemented: ";
            for (Method TF : unimplementedMethods) {
                message += "'" + TF.I.spelling + "', ";
            }
            message = message.substring(0, message.length() - 2);
            handler.reportError(errors[95] + ": %", message, ast.pos);
            return null;
        }

        modules.addImplToTrait(T, ast);
        return null;
    }

    public Object visitMethodList(MethodList ast, Object o) {
        // Validating the trait function names are unique and actually exist
        ast.M.visit(this, o);

        checkMurking(ast.M.attachedStruct);
        checkMurking(ast.M);
        ast.M.setTypeDef();
        ast.M.filename = currentFileName;
        Type MethodT = ast.M.attachedStruct.T;
        if (MethodT.isPointerToStruct()) {
            MethodT = ((PointerType) MethodT).t;
        }
        if (!MethodT.isStruct() || !(((StructType) MethodT).S.I.spelling).equals(currentImpl.getStructType())) {
            String message = "expected " + currentImpl.getStructType() + ", received " + MethodT;
            handler.reportError(errors[97] + ": %", message, ast.M.I.pos);
            return null;
        }

        if (currentImpl.methodExistsOnTrait(ast.M)) {
            if (currentImpl.addTraitFunction(ast.M)) {
                // Function is already implemented
                String message = "method '" + ast.M.I.spelling + "' is already implemented on trait '" + currentImpl.trait.spelling + "'";
                handler.reportError(errors[96] + ": %", message, ast.M.I.pos);
                return null;
            }
        } else {
            String message = "method '" + ast.M.I.spelling + "' does not exist on trait '" + currentImpl.trait.spelling + "'";
            handler.reportError(errors[94] + ": %", message, ast.M.I.pos);
            return null;
        }

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
        System.out.println("CHECKER: GENERIC TYPE");
        return null;
    }

    public Object visitGenericTypeList(GenericTypeList ast, Object o) {
        // Check the trait exists
        if (currentGenericTypes.contains(ast.I.spelling)) {
            handler.reportError(errors[99] + ": %", "generic type '" + ast.I.spelling + "' already exists", ast.I.pos);
            return null;
        }

        currentGenericTypes.add(ast.I.spelling);

        // Checking all the traits really exist
        ast.IL.visit(this, o);

        // Checking the rest of the generic types
        ast.GTL.visit(this, o);

        return null;
    }

    public Object visitEmptyGenericTypeList(EmptyGenericTypeList ast, Object o) {
        return null;
    }

    public Object visitImplementsList(ImplementsList ast, Object o) {
        
        if (!modules.traitExists(ast.I.spelling)) {
            String m = "trait '" + ast.I.spelling + "' for generic function '" + currentGenericFunction.I.spelling + "'";
            handler.reportError(errors[100] + ": %", m, ast.I.pos);
            return null;
        }

        ast.refTrait = modules.getTrait(ast.I.spelling);
        ast.IL.visit(this, o);
        return null;
    }

    public Object visitEmptyImplementsList(EmptyImplementsList ast, Object o) {
        return null;
    }

    public Object visitGenericFunction(GenericFunction ast, Object o) {
        return null;
    }
}
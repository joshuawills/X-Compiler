package X.Nodes;

import java.util.ArrayList;
import java.util.HashMap;

import X.ErrorHandler;
import X.Lexer.Position;

public class Module extends AST {

    public String fileName;

    private ArrayList<Function> functions = new ArrayList<>();

    private ArrayList<Method> methods = new ArrayList<>();

    private ArrayList<GenericFunction> genericFunctions = new ArrayList<>();

    private HashMap<String, GlobalVar> vars = new HashMap<>();
    private HashMap<String, Enum> enums = new HashMap<>();
    private HashMap<String, Struct> structs = new HashMap<>();

    private HashMap<String, Module> importedFiles = new HashMap<>();
    private HashMap<String, Module> usingFiles = new HashMap<>();

    private HashMap<String, Module> aliasToModule = new HashMap<>();

    private boolean isMainModule = false;
    public ErrorHandler thisHandler;

    public boolean isUsed = false;

    public Module(
        String fileNameV,
        HashMap<String, Function> functionsV,
        HashMap<String, GlobalVar> varsV,
        HashMap<String, Enum> enumsV,
        HashMap<String, Struct> structsV,
        boolean isMainModuleV
    ) {
        super(new Position());
        fileName = fileNameV;
        vars = varsV;
        enums = enumsV;
        structs = structsV;
        isMainModule = isMainModuleV;
    }

    public Module(String fileNameV, boolean isMainModuleV) {
        super(new Position());
        isMainModule = isMainModuleV;
        fileName = fileNameV;
    }

    public boolean isMainModule() {
        return isMainModule;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitModule(this, o);
    }

    public void addImportedFile(Module m, String alias) {
        importedFiles.put(m.fileName, m);
        aliasToModule.put(alias, m);
    }

    public boolean importedFileExists(String v) {
        return importedFiles.containsKey(v);
    }

    public void addUsingFile(Module m, String filename) {
        m.isUsed = true;
        usingFiles.put(filename, m);
    }

    public void usingFileExists(String v) {
        usingFiles.containsKey(v);
    }
    
    public void addEnum(Enum e, String filename) {
        e.filename = filename;
        enums.put(e.I.spelling, e);
    }

    public String enumExistsInUsing(String v) {
        for (Module m: usingFiles.values()) {
            if (m.enumExists(v, false)) {
                return m.fileName;
            }
        }
        return "";
    }

    public boolean enumExists(String v, boolean topLevel) {
        if (enums.containsKey(v)) {
            if (topLevel || isMainModule || enums.get(v).isExported) {
                return true;
            }
        }

        if (topLevel) {
            for (Module m: usingFiles.values()) {
                if (m.enumExists(v, false)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean enumExists(String v) {
        return enumExists(v, true);
    }

    public void addStruct(Struct s, String filename) {
        s.filename = filename;
        structs.put(s.I.spelling, s);
    }

    public String structExistsWithUsing(String v) {
        for (Module m: usingFiles.values()) {
            if (m.structExists(v, false)) {
                return m.fileName;
            }
        }
        return "";
    }

    public boolean structExists(String v, boolean topLevel) {
        if (structs.containsKey(v)) {
            if (topLevel || isMainModule || structs.get(v).isExported) {
                return true;
            }
        }

        if (topLevel) {
            for (Module m: usingFiles.values()) {
                if (m.structExists(v, false)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean structExists(String v) {
        return structExists(v, true);
    }

    public void addGlobalVar(GlobalVar v, String filename) {
        v.filename = filename;
        vars.put(v.I.spelling, v);
    }

    public void addFunction(Function f, String filename) {
        f.filename = filename;
        functions.add(f);
    }

    public void addGenericFunction(GenericFunction f, String filename) {
        f.filename = filename;
        genericFunctions.add(f);
    }

    public boolean varExists(String v, boolean topLevel) {
        if (vars.containsKey(v)) {
            if (topLevel || isMainModule || vars.get(v).isExported) {
                return true;
            }
        }

        if (topLevel) {
            for (Module m: usingFiles.values()) {
                if (m.varExists(v, false)) {
                    return true;
                }
            }
        }
       return false;
    }

    public String varExistsInUsing(String v) {
        for (Module m: usingFiles.values()) {
            if (m.varExists(v, false)) {
                return m.fileName;
            }
        }
        return "";
    }

    public boolean varExists(String v) {
        return varExists(v, true);
    }

    public GlobalVar getVar(String v, boolean topLevel) {
        if (vars.containsKey(v)) {
            if (topLevel || isMainModule || vars.get(v).isExported) {
                return vars.get(v);
            }
        }

        if (topLevel) {
            for (Module m: usingFiles.values()) {
                if (m.varExists(v)) {
                    return m.getVar(v, false);
                }
            }
        }
        return null;
    }

    public GlobalVar getVar(String v) {
        return getVar(v, true);
    } 

    public Enum getEnum(String v, boolean topLevel) {
        if (enums.containsKey(v)) {
            if (topLevel || isMainModule || enums.get(v).isExported) {
                return enums.get(v);
            }
        }

        if (topLevel) {
            for (Module m: usingFiles.values()) {
                if (m.enumExists(v)) {
                    return m.getEnum(v, false);
                }
            }
        }

        return null;
    }

    public Enum getEnum(String v) {
        return getEnum(v, true);
    }

    public Struct getStruct(String v, boolean topLevel) {
        if (structs.containsKey(v)) {
            if (topLevel || isMainModule || structs.get(v).isExported) {
                return structs.get(v);
            }
        }

        if (topLevel) {
            for (Module m: usingFiles.values()) {
                if (m.structExists(v)) {
                    return m.getStruct(v, false);
                }
            }
        }

        return null;
    }

    public Struct getStruct(String v) {
        return getStruct(v, true);
    }


    public String functionExistsInUsing(String functionName, List functionParameters) {
        for (Module m: usingFiles.values()) {
            if (m.functionExists(functionName, functionParameters, false)) {
                return m.fileName;
            }
        }
        return "";
    }

    public String genericFunctionExistsInUsing(String genericFunctionName, List functionParams) {
        for (Module m: usingFiles.values()) {
            if (m.genericFunctionExists(genericFunctionName, functionParams, false)) {
                return m.fileName;
            }
        }
        return "";
    }

    public boolean functionExists(String functionName, List functionParameters, boolean topLevel) {
        for (Function f: functions) {
            if (f.I.spelling.equals(functionName) && f.equalTypeParameters(functionParameters)) {
                if (topLevel || isMainModule || f.isExported) {
                    return true;
                }
            }
        }

        if (topLevel) {
            for (Module m: usingFiles.values()) {
                if (m.functionExists(functionName, functionParameters, false)) {
                    return true;
                }
            }
        }

        return false;
    }

    // What makes two generic functions identical

    // They have the same name, and parameters
    //     - with generic parameters, they have the same trait bounds
    public boolean genericFunctionExists(String functionName, List functionParameters, boolean topLevel) {
        for (GenericFunction GF: genericFunctions) {
            if (GF.I.spelling.equals(functionName) && GF.equalTypeParameters(functionParameters)) {
                if (topLevel || isMainModule || GF.isExported) {
                    return true;
                }
            }
        }

        if (topLevel) {
            for (Module m: usingFiles.values()) {
                if (m.genericFunctionExists(functionName, functionParameters, false)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean genericFunctionExists(String functionName, List functionParameters) {
        return genericFunctionExists(functionName, functionParameters, true);
    }

    public boolean functionExists(String functionName, List functionParameters) {
        return functionExists(functionName, functionParameters, true);
    }

    public boolean functionExistsNotExported(String v, List PL) {
        for (Module M: usingFiles.values()) {
            for (Function f: M.functions) {
                if (f.I.spelling.equals(v) && f.equalTypeParameters(PL)) {
                    if (!f.isExported) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean functionWithNameExists(String v, boolean topLevel) {
        for (Function f: functions) {
            if (f.I.spelling.equals(v)) {
                if (topLevel || isMainModule || f.isExported) {
                    return true;
                }
            }
        }

        if (topLevel) {
            for (Module m: usingFiles.values()) {
                if (m.functionWithNameExists(v, false)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean functionWithNameExists(String v) {
        return functionWithNameExists(v, true);
    }


    public Function getFunction(String v, List PL, boolean topLevel) {
        for (Function f: functions) {
            if (f.I.spelling.equals(v) && f.equalTypeParameters(PL)) {
                if (topLevel || isMainModule || f.isExported) {
                    return f;
                }
            }
        }

        if (topLevel) {
            for (Module m: usingFiles.values()) {
                if (m.functionExists(v, PL, false)) {
                    return m.getFunction(v, PL, false);
                }
            }
        }
        return null;
    }

    public Function getFunction(String v, List PL) {
        return getFunction(v, PL, true);
    }

    public HashMap<String, Struct> getStructs() {
        return structs;
    }

    public HashMap<String, GlobalVar> getVars() {
        return vars;
    }

    public ArrayList<Function> getFunctions() {
        return functions;
    }

    public ArrayList<Method> getMethods() {
        return methods;
    }

    public HashMap<String, Enum> getEnums() {
        return enums;
    }

    public boolean aliasExists(String alias) {
        return aliasToModule.containsKey(alias);
    }

    public Module getModuleFromAlias(String alias) {
        return aliasToModule.get(alias);
    }

    public void printAllFunctions() {
        for (Function f : functions) {
            System.out.println(f.I.spelling);
        }
    }

    public void addMethod(Method m) {
        methods.add(m);
    }

    public boolean methodExists(String v, Type T, List PL, boolean topLevel) {
        for (Method m: methods) {
            if (m.I.spelling.equals(v) && m.equalTypeParameters(PL) && m.attachedStruct.T.equals(T)) {
                if (topLevel || isMainModule || m.isExported) {
                    return true;
                }
            }
        }

        // Check the imported files
        for (Module m: importedFiles.values()) {
            if (m.methodExists(v, T, PL, false)) {
                return true;
            }
        }

        // Check the using files
        if (topLevel) {
            for (Module m: usingFiles.values()) {
                if (m.methodExists(v, T, PL, false)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean methodExists(String v, Type T, List PL) {
        return methodExists(v, T, PL, true);
    }

    public boolean methodWithNameExists(String v, Type T, boolean topLevel) {
        for (Method m: methods) {
            if (m.I.spelling.equals(v) && m.attachedStruct.T.equals(T)) {
                return true;
            }
        }

        // Check the import files
        for (Module m: importedFiles.values()) {
            if (m.methodWithNameExists(v, T, false)) {
                return true;
            }
        }

        if (topLevel) {
            for (Module m: usingFiles.values()) {
                if (m.methodWithNameExists(v, T, false)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean methodExistsNotExported(String v, Type T, List PL) {

        // Check imported files
        for (Module M: importedFiles.values()) {
            for (Method m: M.methods) {
                if (m.I.spelling.equals(v) && m.equalTypeParameters(PL) && m.attachedStruct.T.equals(T)) {
                    if (!m.isExported) {
                        return true;
                    }
                }
            }
        }

        for (Module M: usingFiles.values()) {
            for (Method m: M.methods) {
                if (m.I.spelling.equals(v) && m.equalTypeParameters(PL) && m.attachedStruct.T.equals(T)) {
                    if (!m.isExported) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean methodWithNameExists(String v, Type T) {
        return methodWithNameExists(v, T, true);
    }

    public Method getMethod(String v, Type T, List PL, boolean topLevel) {
        for (Method m: methods) {
            if (m.I.spelling.equals(v) && m.equalTypeParameters(PL) && m.attachedStruct.T.equals(T)) {
                if (topLevel || isMainModule || m.isExported) {
                    return m;
                }
            }
        }

        // Check the imported files
        for (Module m: importedFiles.values()) {
            if (m.methodExists(v, T, PL, false)) {
                return m.getMethod(v, T, PL, false);
            }
        }

        if (topLevel) {
            for (Module m: usingFiles.values()) {
                if (m.methodExists(v, T, PL, false)) {
                    return m.getMethod(v, T, PL, false);
                }
            }
        }

        return null;
    }

    public Method getMethod(String v, Type T, List PL) {
        return getMethod(v, T, PL, true);
    }
}

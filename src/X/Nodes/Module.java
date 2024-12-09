package X.Nodes;

import java.util.ArrayList;
import java.util.HashMap;

import X.ErrorHandler;
import X.Lexer.Position;

public class Module extends AST {

    public String fileName;

    private ArrayList<Function> functions = new ArrayList<>();
    private ArrayList<Method> methods = new ArrayList<>();

    private HashMap<String, GlobalVar> vars = new HashMap<>();
    private HashMap<String, Enum> enums = new HashMap<>();
    private HashMap<String, Struct> structs = new HashMap<>();
    private HashMap<String, Module> importedFiles = new HashMap<>();

    private HashMap<String, Module> aliasToModule = new HashMap<>();
    private boolean isMainModule = false;
    public ErrorHandler thisHandler;
    
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
    
    public void addEnum(Enum e) {
        enums.put(e.I.spelling, e);
    }

    public boolean enumExists(String v) {
        return enums.containsKey(v);
    }

    public void addStruct(Struct s) {
        structs.put(s.I.spelling, s);
    }

    public boolean structExists(String v) {
        return structs.containsKey(v);
    }

    public void addGlobalVar(GlobalVar v) {
        vars.put(v.I.spelling, v);
    }

    public void addFunction(Function f) {
        functions.add(f);
    }

    public void addMethod(Method m) {
        methods.add(m);
    }

    public boolean entityExists(String v) {
        return vars.containsKey(v) || functionWithNameExists(v)
            || enums.containsKey(v) || structs.containsKey(v);
    }

    public boolean varExists(String v) {
        return vars.containsKey(v);
    }

    public GlobalVar getVar(String v) {
        return vars.get(v);
    }

    public Enum getEnum(String v) {
        return enums.get(v);
    }

    public Struct getStruct(String v) {
        return structs.get(v);
    }

    public boolean functionExists(String v, List PL) {
        for (Function f: functions) {
            if (f.I.spelling.equals(v) && f.equalTypeParameters(PL)) {
                return true;
            }
        }
        return false;
    }

    public boolean methodExists(String v, Type T, List PL) {
        for (Method m: methods) {
            if (m.I.spelling.equals(v) && m.equalTypeParameters(PL) && m.attachedStruct.T.equals(T)) {
                return true;
            }
        }
        return false;
    }

    public boolean functionWithNameExists(String v) {
        for (Function f: functions) {
            if (f.I.spelling.equals(v)) {
                return true;
            }
        }
        return false;
    }

    public boolean methodWithNameExists(String v, Type T) {
        for (Method m: methods) {
            if (m.I.spelling.equals(v) && m.attachedStruct.T.equals(T)) {
                return true;
            }
        }
        return false;
    }

    public Function getFunction(String v, List PL) {
        for (Function f: functions) {
            if (f.I.spelling.equals(v) && f.equalTypeParameters(PL)) {
                return f;
            }
        }
        return null;
    }

    public Method getMethod(String v, Type T, List PL) {
        for (Method m: methods) {
            if (m.I.spelling.equals(v) && m.equalTypeParameters(PL) && m.attachedStruct.T.equals(T)) {
                return m;
            }
        }
        return null;
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

}

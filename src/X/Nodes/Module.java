package X.Nodes;

import java.util.HashMap;

import X.Lexer.Position;

public class Module extends AST {

    public String fileName;
    private HashMap<String, Function> functions = new HashMap<>();
    private HashMap<String, GlobalVar> vars = new HashMap<>();
    private HashMap<String, Enum> enums = new HashMap<>();
    private HashMap<String, Struct> structs = new HashMap<>();
    
    public Module(
        String fileNameV,
        HashMap<String, Function> functionsV,
        HashMap<String, GlobalVar> varsV,
        HashMap<String, Enum> enumsV,
        HashMap<String, Struct> structsV
    ) {
        super(new Position());
        fileName = fileNameV;
        vars = varsV;
        enums = enumsV;
        structs = structsV;
    }

    public Module(String fileNameV) {
        super(new Position());
        fileName = fileNameV;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitModule(this, o);
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
        functions.put(f.I.spelling + "." + f.TypeDef, f);
    }

    public boolean entityExists(String v) {
        return vars.containsKey(v) || functions.containsKey(v)
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

    public boolean functionExists(String v) {
        return functions.containsKey(v);
    }

    public boolean functionWithNameExists(String v) {
        for (String key : functions.keySet()) {
            if (key.split("\\.")[0].equals(v)) {
                return true;
            }
        }
        return false;
    }

    public Function getFunction(String v) {
        return functions.get(v);
    }

    public HashMap<String, Struct> getStructs() {
        return structs;
    }

    public HashMap<String, GlobalVar> getVars() {
        return vars;
    }

    public HashMap<String, Function> getFunctions() {
        return functions;
    }

}

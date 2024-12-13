package X;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;

import X.Nodes.Function;
import X.Nodes.GlobalVar;
import X.Nodes.List;
import X.Nodes.Method;
import X.Nodes.Module;
import X.Nodes.TupleType;
import X.Nodes.Type;

public class AllModules {


    private ArrayList<Function> libCFunctions = new ArrayList<Function>();
    private ArrayList<GlobalVar> libCVariables = new ArrayList<GlobalVar>();

    private ArrayList<Module> modules = new ArrayList<Module>();

    private ArrayList<Method> methods = new ArrayList<>();

    private static AllModules instance = null;

    private ArrayList<TupleType> tupleTyples = new ArrayList<>();

    public int strCount = 0;
    public final HashMap<String, Integer> stringConstantsMapping = new HashMap<>();

    public Path libPath;

    private AllModules() {
        String val = System.getenv("X_LIB_PATH");
        if (val != null) {
            libPath = Path.of(val);
        } else {
            Path homePath = Path.of(System.getProperty("user.home"));
            libPath = homePath.resolve(".x-lib");
        }
    }

    public static AllModules getInstance() {
        if (instance == null) {
            instance = new AllModules();
        }
        return instance;
    }

    public ArrayList<Module> getModules() {
        return modules;
    }

    public void addModule(Module mainModule) {
        modules.add(mainModule);
    }

    public void addMethod(Method m) {
        methods.add(m);
    }

    public void addTupleType(TupleType tupleType) {
        tupleTyples.add(tupleType);
    }

    public boolean tupleTypeExists(TupleType tupleType) {
        return tupleTyples.contains(tupleType);
    }

    public int getTupleTypeIndex(TupleType tupleType) {
        return tupleTyples.indexOf(tupleType);
    }

    public ArrayList<TupleType> getTupleTypes() {
        return tupleTyples;
    }

    public void addLibCFunction(Function f) {
        libCFunctions.add(f);
    }

    public void addLibCVariable(GlobalVar v) {
        libCVariables.add(v);
    }

    public boolean libCFunctionExists(String name) {
        for (Function f : libCFunctions) {
            if (f.I.spelling.equals(name)) {
                return true;
            }
        }
        return false;
    }

    public boolean libCVariableExists(String name) {
        for (GlobalVar v : libCVariables) {
            if (v.I.spelling.equals(name)) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<Function> getLibCFunctions() {
        return libCFunctions;
    }

    public ArrayList<GlobalVar> getLibCVariables() {
        return libCVariables;
    }

    public Function getLibCFunction(String name) {
        for (Function f : libCFunctions) {
            if (f.I.spelling.equals(name)) {
                return f;
            }
        }
        return null;
    }

    public GlobalVar getLibCVariable(String name) {
        for (GlobalVar v : libCVariables) {
            if (v.I.spelling.equals(name)) {
                return v;
            }
        }
        return null;
    }

    public boolean moduleExists(String filename) {
        for (Module m : modules) {
            if (m.fileName.equals(filename)) {
                return true;
            }
        }
        return false;
    }

    public Module getModule(String filename) {
        for (Module m : modules) {
            if (m.fileName.equals(filename)) {
                return m;
            }
        }
        return null;
    }

    public Module getMainModule() {
        for (Module m : modules) {
            if (m.isMainModule()) {
                return m;
            }
        }
        return null;
    }

    public boolean methodExists(String v, Type T, List PL) {
        for (Method m: methods) {
            if (m.I.spelling.equals(v) && m.equalTypeParameters(PL) && m.attachedStruct.T.equals(T)) {
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

    public Method getMethod(String v, Type T, List PL) {
        for (Method m: methods) {
            if (m.I.spelling.equals(v) && m.equalTypeParameters(PL) && m.attachedStruct.T.equals(T)) {
                return m;
            }
        }
        return null;
    }

    public ArrayList<Method> getMethods() {
        return methods;
    }
    
}

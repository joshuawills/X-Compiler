package X;

import java.util.ArrayList;
import java.util.HashMap;

import X.Nodes.Function;
import X.Nodes.Module;
import X.Nodes.TupleType;

public class AllModules {


    private ArrayList<Function> libCFunctions = new ArrayList<Function>();

    private ArrayList<Module> modules = new ArrayList<Module>();
    private static AllModules instance = null;

    private ArrayList<TupleType> tupleTyples = new ArrayList<>();

    public int strCount = 0;
    public final HashMap<String, Integer> stringConstantsMapping = new HashMap<>();

    private AllModules() {
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

    public boolean libCFunctionExists(String name) {
        for (Function f : libCFunctions) {
            if (f.I.spelling.equals(name)) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<Function> getLibCFunctions() {
        return libCFunctions;
    }

    public Function getLibCFunction(String name) {
        for (Function f : libCFunctions) {
            if (f.I.spelling.equals(name)) {
                return f;
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

    
}

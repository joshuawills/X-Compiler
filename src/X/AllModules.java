package X;

import java.util.ArrayList;
import X.Nodes.Module;

public class AllModules {


    private ArrayList<Module> modules = new ArrayList<Module>();
    private static AllModules instance = null;

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

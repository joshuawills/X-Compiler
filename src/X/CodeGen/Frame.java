package X.CodeGen;

import java.util.Stack;

public class Frame {

    private final boolean isMain;
    private int label;
    private int localVarIndex;
    public int currentStack;
    public int maxStack;
    public Stack<String> conStack;
    public Stack<String> brkStack;
    public Stack<String> scopeStart;
    public Stack<String> scopeEnd;

    public Frame(boolean isMain) {
        this.isMain = isMain;
        label = localVarIndex = currentStack = maxStack = 0;
        conStack = new Stack<>();
        brkStack = new Stack<>();
        scopeStart = new Stack<>();
        scopeEnd = new Stack<>();
    }

    public boolean isMain() {
        return this.isMain;
    }

    public int getNewIndex() {
        return localVarIndex++;
    }

    public String getNewLabel() {
        return "L" + label++;
    }

    public void push() {
        push(1);
    }

    public void push(int i) {
        currentStack += i;
        if (currentStack > maxStack) {
            maxStack = currentStack;
        }
    }

    public void pop() {
        pop(1);
    }

    public void pop(int i) {
        currentStack -= i;
        if (currentStack < 0) {
            System.out.println("YOU'VE POPPED TOO MUCH");
            System.exit(1);
        }
    }

}
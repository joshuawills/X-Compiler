package X.CodeGen;

import java.io.PrintWriter;

public class Instruction {

    public String instr;

    public Instruction(String i) {
        instr = i;
    }

    public void write(PrintWriter writer) {
        writer.print(instr);
    }
}

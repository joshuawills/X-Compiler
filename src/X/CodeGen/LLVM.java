package X.CodeGen;

import java.io.FileOutputStream;
import java.io.PrintWriter;

public class LLVM {

    public static int nextInstAddr = 0;
    public static int codeSize = 512;
    public static Instruction[] code = new Instruction[codeSize];

    public static void append(Instruction i) {
        if (nextInstAddr >= codeSize) {
            Instruction[] newCode = new Instruction[2 * codeSize];
            System.arraycopy(code, 0, newCode, 0, codeSize);
            codeSize = 2 * code.length;
            code = newCode;
        }
        code[nextInstAddr++] = i;
    }

    public static void dump(String filename) {
        PrintWriter writer;
        try {
            writer = new PrintWriter(new FileOutputStream(filename));
            for (int addr = 0; addr < nextInstAddr; addr++) {
                LLVM.code[addr].write(writer);
            }
            writer.close();
        } catch (Exception e) {
            System.out.println(e);
            System.exit(1);
        }
    }

    public static final String CHAR_TYPE = "i8";
    public static final String INT_TYPE = "i32";
    public static final String FLOAT_TYPE = "float";
    public static final String BOOL_TYPE = "i1";
    public static final String VOID_TYPE = "void";


}

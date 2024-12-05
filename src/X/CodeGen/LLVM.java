package X.CodeGen;

import java.io.FileOutputStream;
import java.io.PrintWriter;

public class LLVM {

    public static int nextInstAddr = 0;
    public static int nextInstAddrConst = 0;
    public static int codeSize = 512;
    public static int codeSizeConst = 513;
    public static Instruction[] code = new Instruction[codeSize];
    public static Instruction[] constant = new Instruction[codeSize];

    public static void append(Instruction i) {
        if (nextInstAddr >= codeSize) {
            Instruction[] newCode = new Instruction[2 * codeSize];
            System.arraycopy(code, 0, newCode, 0, codeSize);
            codeSize = 2 * code.length;
            code = newCode;
        }
        code[nextInstAddr++] = i;
    }

    public static void appendConstant(Instruction i) {
        if (nextInstAddrConst >= codeSizeConst) {
            Instruction[] newCode = new Instruction[2 * codeSizeConst];
            System.arraycopy(constant, 0, newCode, 0, codeSizeConst);
            codeSizeConst = 2 * constant.length;
            constant= newCode;
        }
        constant[nextInstAddrConst++] = i;
    }

    public static void dump(String filename) {
        PrintWriter writer;
        try {
            writer = new PrintWriter(new FileOutputStream(filename));
            for (int addr = 0; addr < nextInstAddrConst; addr++) {
                LLVM.constant[addr].write(writer);
            }
            for (int addr = 0; addr < nextInstAddr; addr++) {
                LLVM.code[addr].write(writer);
            }
            writer.close();
        } catch (Exception e) {
            System.out.println(e);
            System.exit(1);
        }
    }

    public static final String I64_TYPE = "i64";
    public static final String I32_TYPE = "i32";
    public static final String I8_TYPE = "i8";
    public static final String F32_TYPE = "float";
    public static final String F64_TYPE = "double";
    public static final String BOOL_TYPE = "i1";
    public static final String VOID_TYPE = "void";
    public static final String VARIATIC_TYPE = "...";


}

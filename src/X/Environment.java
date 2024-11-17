package X;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import X.Nodes.Function;
import X.Nodes.Type;

public class Environment {

    public static Type booleanType, i64Type, strType, voidType, errorType, f32Type, f64Type, i8Type, charPointerType, i32Type;
    public static Function outI64, outF32, outF64, outChar, outStr, malloc, free;

    public static List<String> functionNames = new ArrayList<>(Arrays.asList("outI64", "outF32", "outF64", "outChar", "outStr", "malloc", "free"));

}

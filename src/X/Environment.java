package X;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import X.Nodes.Function;
import X.Nodes.Type;

public class Environment {

    public static Type booleanType, i64Type, strType, voidType, errorType, floatType, charType, charPointerType, i32Type;
    public static Function outI64, outFloat, outChar, outStr, malloc, free;

    public static List<String> functionNames = new ArrayList<>(Arrays.asList("outI64", "outFloat", "outChar", "outStr", "malloc", "free"));

}

package X;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import X.Nodes.Function;
import X.Nodes.Type;

public class Environment {

    public static Type booleanType, i64Type, strType, voidType, errorType, floatType, charType, charPointerType;
    public static Function outInt, outFloat, outChar, outStr, malloc, free;

    public static List<String> functionNames = new ArrayList<>(Arrays.asList("outInt", "outFloat", "outChar", "outStr", "malloc", "free"));

}

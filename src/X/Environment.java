package X;

import X.Nodes.Function;
import X.Nodes.GlobalVar;
import X.Nodes.Type;

public class Environment {

    public static Type booleanType, i64Type, strType, voidType, errorType, f32Type, f64Type, i8Type, charPointerType, i32Type, voidPointerType, variaticType;
    public static Function 
        sin, cos, pow, malloc, free, calloc, realloc, exit, fmod, fabs, printf, __isoc99_scanf, fgets, getchar, memcmp
    ;

    public static GlobalVar stdin;

}

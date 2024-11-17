// Testing types using the type() command

struct Data -> {
    x: i32
}

fn main() -> i64 {

    // Testing default types of expressions
    outStr(type(true)); // bool
    outStr(type(false)); // bool
    outStr(type(1)); // i64
    outStr(type(1.0)); // f64
    outStr(type('c')); // i8

    let x: i64 = 1;
    outStr(type(x)); // i64

    let x1: i32= 1;
    outStr(type(x1)); // i32

    let x2: i8 = 1;
    outStr(type(x2)); // i8

    outStr(type(x1 + 19)); // i64 - when there's no clear type, it defaults to i64

    let y1: f32 = 1.0;
    outStr(type(y1)); // f32

    let y2: f64 = 1.0;
    outStr(type(y2)); // f64

    outStr(type(y1 + 19.0)); // f64 - when there's no clear type, it defaults to f64
    
    let a = [1, 2, 3, 4, 5];
    outStr(type(a[0]));

    let b = Data { 21 };
    outStr(type(b.x));

}
// Testing types using the type() command

import "../../lib/std.x" as std;

struct Data -> {
    x: i32
}

fn main() -> void {

    // Testing default types of expressions
    std::print(type(true)); // bool
    std::print(type(false)); // bool
    std::print(type(1)); // i64
    std::print(type(1.0)); // f64
    std::print(type('c')); // i8

    let mut x: i64 = 1;
    std::print(type(x)); // i64
    std::print(type(&x)); // i64

    let mut x1: i32= 1;
    std::print(type(x1)); // i32
    std::print(type(&x1)); // i32

    let x2: i8 = 1;
    std::print(type(x2)); // i8

    std::print(type(x1 + 19)); // i64 - when there's no clear type, it defaults to i64

    let y1: f32 = 1.0;
    std::print(type(y1)); // f32

    let y2: f64 = 1.0;
    std::print(type(y2)); // f64

    std::print(type(y1 + 19.0)); // f64 - when there's no clear type, it defaults to f64
    
    let a = [1, 2, 3, 4, 5];
    std::print(type(a[0]));

    let b = Data { 21 };
    std::print(type(b.x));

}

// Testing types using the type() command

import std, io;

struct Data -> {
    x: i32
}

fn main() -> void {

    // Testing default types of expressions
    io::print(type(true)); // bool
    io::print(type(false)); // bool
    io::print(type(1)); // i64
    io::print(type(1.0)); // f64
    io::print(type('c')); // i8

    let mut x: i64 = 1;
    io::print(type(x)); // i64
    io::print(type(&x)); // i64

    let mut x1: i32= 1;
    io::print(type(x1)); // i32
    io::print(type(&x1)); // i32

    let x2: i8 = 1;
    io::print(type(x2)); // i8

    io::print(type(x1 + 19)); // i64 - when there's no clear type, it defaults to i64

    let y1: f32 = 1.0;
    io::print(type(y1)); // f32

    let y2: f64 = 1.0;
    io::print(type(y2)); // f64

    io::print(type(y1 + 19.0)); // f64 - when there's no clear type, it defaults to f64
    
    let a = [1, 2, 3, 4, 5];
    io::print(type(a[0]));

    let b = Data { 21 };
    io::print(type(b.x));

}

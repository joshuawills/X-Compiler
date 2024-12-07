// Factorial

import std, io;

fn main() -> void {
    let x: i64 = 5;
    io::println(factorial(x));
}

fn factorial(x: i64) -> i64 {
    if x <= 1 {
        return 1;
    }
    return x * factorial(x - 1);
}

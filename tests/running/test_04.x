// Messing around with functions and function overloading

import std, io;

fn add() -> i64 {
    return add(1, 2);
}

fn add(x: i64) -> i64 {
    return add(x, 0);
}

fn add(x: i64, y: i64) -> i64 {
    return x + y;
}

fn main() -> void {
    io::println(add()); // 3
    io::println(add(2)); // 2
    io::println(add(2, 2)); // 4
}
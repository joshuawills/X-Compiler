// Swap POINTERS

import std, io;

fn swap(mut a: i64*, mut b: i64*) -> void {
    let temp: i64 = *a;
    *a = *b;
    *b = temp;
}

fn main() -> void {
    let mut a: i64 = 2;
    let mut b: i64 = 3;
    swap(&a, &b);
    io::println(a);
    io::println(b);
}
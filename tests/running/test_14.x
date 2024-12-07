// Euclid

import std, io;

fn main() -> void {

    let x: i64 = 6;
    let y: i64 = 2;

    if x <= 0 || y <= 0 {
        return;
    }

    io::println(euclid(x, y));
    return;
}

fn euclid(x: i64, y: i64) -> i64 {
    if x == 0 {
        return y;
    }
    return euclid(y % x, x);
}

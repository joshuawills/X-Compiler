// operator precedence and maths

import std, io;

fn main() -> void {

    let x: i64 = 19;
    let y: i64 = 21;

    io::println(2 + x - 19);
    io::println(19 * y % x);
    io::println(x + y * 2);
    io::println((x + y) * 2);
    io::println(x - -1);
    io::println(-y * -19);
    io::println(x + y - 21 * 41 / 3);
}
// operator precedence and maths

import std;

fn main() -> void {

    let x: i64 = 19;
    let y: i64 = 21;

    std::println(2 + x - 19);
    std::println(19 * y % x);
    std::println(x + y * 2);
    std::println((x + y) * 2);
    std::println(x - -1);
    std::println(-y * -19);
    std::println(x + y - 21 * 41 / 3);
}
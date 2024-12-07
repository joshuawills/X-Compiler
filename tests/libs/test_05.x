// Basic IO operations

import std;

fn main() -> void {

    let num = std::read_i64();
    std::println(num);

    let mut buffer: i8[100];
    std::read_str(buffer, 100);
    std::println(buffer);

    let char = std::read_char();
    std::println(char);

    let num2 = std::read_i32();
    std::println(num2);

    let num3 = std::read_f64();
    std::println(num3);

}
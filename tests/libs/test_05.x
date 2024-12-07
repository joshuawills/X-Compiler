// Basic IO operations

import std, io;

fn main() -> void {

    let num = io::read_i64();
    io::println(num);

    let mut buffer: i8[100];
    io::read_str(buffer, 100);
    io::println(buffer);

    let char = io::read_char();
    io::println(char);

    let num2 = io::read_i32();
    io::println(num2);

    let num3 = io::read_f64();
    io::println(num3);

}
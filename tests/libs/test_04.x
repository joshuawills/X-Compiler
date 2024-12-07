// Printing different things

import std, io;

fn main() -> void {

    io::println("A string");
    io::print("A string");

    io::print(3.14159265);
    io::println(2.71828182);

    io::print(true);
    io::println(false);

    io::print(3.14159265 as f32);
    io::println(2.71828182 as f32);

    io::print(true);
    io::println(false);

    io::print(3);
    io::println(2);


    io::print(3 as i32);
    io::println(2 as i32);

    io::print('A');
    io::println('B');

}
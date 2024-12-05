// Printing different things

import "../../lib/std.x" as std;

fn main() -> void {

    std::println("A string");
    std::print("A string");

    std::print(3.14159265);
    std::println(2.71828182);

    std::print(true);
    std::println(false);

    std::print(3.14159265 as f32);
    std::println(2.71828182 as f32);

    std::print(true);
    std::println(false);

    std::print(3);
    std::println(2);


    std::print(3 as i32);
    std::println(2 as i32);

    std::print('A');
    std::println('B');

}
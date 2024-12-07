// Testing power in the math lib

import std, io, math;

fn main() -> void {

    io::println(Math::power(2.0, 3.0));
    io::println(Math::power(2.0, 0.0));
    io::println(Math::power(2.0, 0.5));
    io::println(Math::power(2.0, -1.0));

}
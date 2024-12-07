// Basic file import work

import std, io;
import "math-what.x" as math;

fn main() -> void {
    io::println(math::add(1, 2));

    io::println(math::Zero);

    let val = math::Vec2 { 1, 1 };
    io::println(val.x);

}
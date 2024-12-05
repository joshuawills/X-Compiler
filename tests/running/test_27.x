// Basic file import work

import "../../lib/std.x" as std;
import "math-what.x" as math;

fn main() -> void {
    std::println(math::add(1, 2));

    std::println(math::Zero);

    let val = math::Vec2 { 1, 1 };
    std::println(val.x);

}
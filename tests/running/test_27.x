// Basic file import work

import "math-what.x" as math;

fn main() -> void {
    outI64(math::add(1, 2));

    outI64(math::Zero);

    let val = math::Vec2 { 1, 1 };
    outI64(val.x);

}
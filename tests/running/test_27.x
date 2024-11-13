// Basic file import work

import "../../lib/math.x" as math;

fn main() -> int {
    outInt(math::add(1, 2));

    outInt(math::Zero);

    let val = math::Vec2 { 1, 1 };
    outInt(val.x);

    return 0;
}
// Basic file import work

import "../../lib/math.x" as math;

fn main() -> i64 {
    outI64(math::add(1, 2));

    outI64(math::Zero);

    let val = math::Vec2 { 1, 1 };
    outI64(val.x);

    return 0;
}
// Enums in other files accessed!

import "../../lib/std.x" as std;

import "math-what.x" as Math;

fn main() -> void {

        let mut val = Math::Boolean.TRUE;

        if val == Math::Boolean.TRUE {
                std::println(1);
        }

        val = Math::Boolean.FALSE;

        if val == Math::Boolean.FALSE {
                std::println(1);
        }
}

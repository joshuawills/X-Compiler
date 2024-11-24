// Enums in other files accessed!

import "math-what.x" as Math;

fn main() -> void {

        let mut val = Math::Boolean.TRUE;

        if val == Math::Boolean.TRUE {
                outI64(1);
        }

        val = Math::Boolean.FALSE;

        if val == Math::Boolean.FALSE {
                outI64(1);
        }
}

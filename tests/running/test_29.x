// Enums in other files accessed!

import "../../lib/math.x" as Math;

fn main() -> i64 {

        let mut val = Math::Boolean.TRUE;

        if val == Math::Boolean.TRUE {
                outInt(1);
        }

        val = Math::Boolean.FALSE;

        if val == Math::Boolean.FALSE {
                outInt(1);
        }
}

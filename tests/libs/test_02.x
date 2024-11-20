// Testing power in the math lib

import "../../lib/math.x" as Math;

fn main() -> i64 {

    outF64(Math::power(2.0, 3.0));
    outF64(Math::power(2.0, 0.0));
    outF64(Math::power(2.0, 0.5));
    outF64(Math::power(2.0, -1.0));

}
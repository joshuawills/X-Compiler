// Testing power in the math lib

import "../../lib/math.x" as Math;
import "../../lib/std.x" as std;

fn main() -> void {

    std::println(Math::power(2.0, 3.0));
    std::println(Math::power(2.0, 0.0));
    std::println(Math::power(2.0, 0.5));
    std::println(Math::power(2.0, -1.0));

}
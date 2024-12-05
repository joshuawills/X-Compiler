// If conditional is not boolean

import "../../lib/std.x" as std;

fn main() -> void {

    if 1 {
        std::println(1);
    }

    if 'c' {
      std::println(1);
    }

}
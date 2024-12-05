// Logging global arrays, mutating them

import "../../lib/std.x" as std;

let x: i64[] = [1, 19, 9];
let x1: bool[] = [true, false, true];
let mut x2: i8[] = ['a', 'b', 'c'];

fn main() -> void {

    loop i in 3 {
        std::println(x[i]);
    }

    loop y in 3 {
        if x1[y] {
            std::println(1);
        } else {
            std::println(0);
        }
    }

    loop z in 3 {
        std::println(x2[z]);
    }

    loop a in 3 {
        x2[a] = 'z';
    }

    loop b in 3 {
        std::println(x2[b]);
    }
}
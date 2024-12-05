// Do array index operations on a pointer

import "../../lib/std.x" as std;

fn main() -> void { 

    let mut x = [1, 2, 3, 4, 5];

    let y: i64* = x;
    
    loop i in 5 {
        std::println(y[i]);
    }
}
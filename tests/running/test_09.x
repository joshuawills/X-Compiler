// Emitting type annotations

import std, io;

enum Boolean -> { TRUE, FALSE }

let x = 21;
let y = true;
let z = 'a';
let a = Boolean.TRUE;

fn main() -> void {

    if y {
        io::println(x);
        io::println(z);
    }

    let arr = [1, 2, 3, 4, 5, 6];

    loop i in 6 {
        io::println(arr[i]);
    }

    // Casts all following types to the first one where possible
    let arr2 = [1, 'a', 2];

    loop i in 3 {
        io::println(arr2[i]);
    }

}

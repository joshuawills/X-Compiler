// Do array index operations on a pointer

import std, io;

fn main() -> void { 

    let mut x = [1, 2, 3, 4, 5];

    let y: i64* = x;
    
    loop i in 5 {
        io::println(y[i]);
    }
}
// Do array index operations on a pointer -> mutable var

import std, io;

fn main() -> void { 

    let mut x = [1, 2, 3, 4, 5];

    let mut y: i64* = null;
    y = x;
    
    loop i in 5 {
        io::println(y[i]);
    }
}
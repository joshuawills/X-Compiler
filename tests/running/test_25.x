// Redeclaring char*

import std, io;

fn main() -> void {
    let mut val = "hello, world\n";

    io::print(val);
    
    val = "goodbye, world!\n";

    io::print(val);

}

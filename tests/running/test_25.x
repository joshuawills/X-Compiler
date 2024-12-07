// Redeclaring char*

import std;

fn main() -> void {
    let mut val = "hello, world\n";

    std::print(val);
    
    val = "goodbye, world!\n";

    std::print(val);

}

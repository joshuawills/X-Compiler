// Simple struct accesses, no nesting

import std, io;

struct Box -> {
    num: i64,
    cond: bool
}

fn main() -> void {

    let var = Box { 2, false };

    if !var.cond {
        io::println(var.num);
    }

}

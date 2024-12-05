// Simple struct accesses, no nesting

import "../../lib/std.x" as std;

struct Box -> {
    num: i64,
    cond: bool
}

fn main() -> void {

    let var = Box { 2, false };

    if !var.cond {
        std::println(var.num);
    }

}

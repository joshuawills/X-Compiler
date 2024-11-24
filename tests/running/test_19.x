// Simple struct accesses, no nesting

struct Box -> {
    num: i64,
    cond: bool
}

fn main() -> void {

    let var = Box { 2, false };

    if !var.cond {
        outI64(var.num);
    }

}

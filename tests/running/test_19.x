// Simple struct accesses, no nesting

struct Box -> {
    num: i64,
    cond: bool
}

fn main() -> i64 {

    let var = Box { 2, false };

    if !var.cond {
        outI64(var.num);
    }

    return 0;

}

// Simple struct accesses, no nesting

struct Box -> {
    num: int,
    cond: bool
}

fn main() -> int {

    let var = Box { 2, false };

    if !var.cond {
        outInt(var.num);
    }

    return 0;

}

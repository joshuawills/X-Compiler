// Can't deref a constant

let x: int = -1;

fn main() -> int {

    let y: int* = &x;

}
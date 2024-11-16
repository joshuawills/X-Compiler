// Can't deref a constant

let x: i64 = -1;

fn main() -> i64 {

    let y: i64* = &x;

}
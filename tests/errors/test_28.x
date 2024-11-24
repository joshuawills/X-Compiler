// Can't deref a constant

let x: i64 = -1;

fn main() -> void {

    let y: i64* = &x;

}
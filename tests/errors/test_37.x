// Array index is not an integer

fn main() -> i64 {
    let mut x: i64[] = [1, 2, 3, 4];
    x[true] = 21;
}
// Array index is not an integer

fn main() -> int {
    let mut x: int[] = [1, 2, 3, 4];
    x[true] = 21;
}
// Function declared but never used

fn foo() -> void {

}

fn add(x: i64) -> i64 {
    return add(0, x);
}

fn add(x: i64, y: i64) -> i64 {
    return x + y;
}

fn main() -> void {
    outI64(add(2, 3));
}
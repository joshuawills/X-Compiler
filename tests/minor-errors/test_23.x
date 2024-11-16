// Function declared but never used

fn foo() -> void {

}

fn add(x: i64) -> i64 {
    return add(0, x);
}

fn add(x: i64, y: i64) -> i64 {
    return x + y;
}

fn main() -> i64 {
    outInt(add(2, 3));
}
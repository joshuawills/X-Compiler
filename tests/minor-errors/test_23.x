// Function declared but never used

fn foo() -> void {

}

fn add(x: int) -> int {
    return add(0, x);
}

fn add(x: int, y: int) -> int {
    return x + y;
}

fn main() -> int {
    outInt(add(2, 3));
}
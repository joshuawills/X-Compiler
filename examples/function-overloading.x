fn add(x: int) -> int {
    return add(0, x);
}

fn add(x: int, y: int) -> int {
    return x + y;
}

fn main() -> int {
    outInt(add(2, 1)); // logs 3
    outInt(add(2));   // logs 2
}
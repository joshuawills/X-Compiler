// Messing around with functions and function overloading

fn add() -> int {
    return add(1, 2);
}

fn add(x: int) -> int {
    return add(x, 0);
}

fn add(x: int, y: int) -> int {
    return x + y;
}

fn main() -> int {
    outInt(add()); // 3
    outInt(add(2)); // 2
    outInt(add(2, 2)); // 4
}
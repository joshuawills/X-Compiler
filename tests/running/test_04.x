// Messing around with functions and function overloading

fn add() -> i64 {
    return add(1, 2);
}

fn add(x: i64) -> i64 {
    return add(x, 0);
}

fn add(x: i64, y: i64) -> i64 {
    return x + y;
}

fn main() -> i64 {
    outInt(add()); // 3
    outInt(add(2)); // 2
    outInt(add(2, 2)); // 4
}
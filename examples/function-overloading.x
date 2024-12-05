fn add(x: i64) -> i64 {
    return add(0, x);
}

fn add(x: i64, y: i64) -> i64 {
    return x + y;
}

fn main() -> void {
    std::println(add(2, 1)); // logs 3
    std::println(add(2));   // logs 2
}
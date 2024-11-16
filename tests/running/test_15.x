// Factorial

fn main() -> i64 {
    let x: i64 = 5;
    outInt(factorial(x));
}

fn factorial(x: i64) -> i64 {
    if x <= 1 {
        return 1;
    }
    return x * factorial(x - 1);
}

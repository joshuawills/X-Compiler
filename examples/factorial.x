fn main() -> int {
    let x: int = 5;
    outInt(factorial(x));
}

fn factorial(x: int) -> int {
    if x <= 1 {
        return 1;
    }
    return x * factorial(x - 1);
}

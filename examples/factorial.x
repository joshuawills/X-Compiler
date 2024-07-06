fn main() -> int {
    mut int x;
    inInt("Enter a number: ", x);
    outInt(factorial(x));
}

fn factorial(int x) -> int {
    if x <= 1 {
        return 1;
    }
    return x * factorial(x - 1);
}

fn main() -> int {
    mut int x;
    loop {
        inInt("Enter a number: ", x);
        if !is_prime(x) {
            break;
        }
        outInt(x);
    }
    return 0;
}

fn is_prime(int x) -> bool {
    if x == 0 || x == 1 {
        return false;
    }
    loop i in 2 (x / 2) {
        if x % i == 0 {
            return false;
        }
    }
    return true;
}
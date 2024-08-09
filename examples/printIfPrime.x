fn main() -> int {
    mut int x;
    loop {
        inInt("Enter a number: ", x);
        if is_prime(x) {
            outInt(x);
        } else {
            break;
        }
    }
    return 0;
}

fn is_prime(int x) -> bool {
    if x == 0 || x == 1 {
        return false;
    }
    loop i in 2 (1 + x / 2) {
        if x % i == 0 {
            return false;
        }
    }
    return true;
}
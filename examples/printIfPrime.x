fn main() -> void {
    let mut x;
    loop {
        inInt("Enter a number: ", x);
        if is_prime(x) {
            std::println(x);
        } else {
            break;
        }
    }
}

fn is_prime(x: i64) -> bool {
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
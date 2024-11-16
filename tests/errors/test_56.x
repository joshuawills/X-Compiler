// Dot access impermissible on non variables

struct IntBox -> {
    a: i64
}

fn main() -> i64 {
    let b = IntBox.c;

    return 0;
}
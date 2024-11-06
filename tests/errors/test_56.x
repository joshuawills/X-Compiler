// Dot access impermissible on non variables

struct IntBox -> {
    a: int
}

fn main() -> int {
    let b = IntBox.c;

    return 0;
}
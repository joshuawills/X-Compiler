// Dot access impermissible on non variables

struct IntBox -> {
    a: i64
}

fn main() -> void {
    let b = IntBox.c;

}
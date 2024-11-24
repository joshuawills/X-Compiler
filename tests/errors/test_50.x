// Insufficient arguments to struct declaration

struct DataBox -> {
    a: i64,
    b: i8,
    c: bool
}

fn main() -> void {
    let data = DataBox { 1, 'c' };
}
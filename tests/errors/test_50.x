// Insufficient arguments to struct declaration

struct DataBox -> {
    a: i64,
    b: char,
    c: bool
}

fn main() -> i64 {
    let data = DataBox { 1, 'c' };
}
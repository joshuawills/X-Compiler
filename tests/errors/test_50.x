// Insufficient arguments to struct declaration

struct DataBox -> {
    a: int,
    b: char,
    c: bool
}

fn main() -> int {
    let data = DataBox { 1, 'c' };
}
// Incompatible type for struct member

struct DataBox -> {
    a: i64,
    b: char
}

fn main() -> i64 {
    let data = DataBox { false, 'c' };
    let data2 = DataBox { 2, true };
}

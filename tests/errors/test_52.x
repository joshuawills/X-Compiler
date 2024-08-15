// Incompatible type for struct member

struct DataBox -> {
    a: int,
    b: char
}

fn main() -> int {
    let data = DataBox { false, 'c' };
    let data2 = DataBox { 2, true };
}

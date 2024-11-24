// Incompatible type for struct member

struct DataBox -> {
    a: i64,
    b: i8 
}

fn main() -> void {
    let data = DataBox { false, 'c' };
    let data2 = DataBox { 2, true };
}

// Incompatible types for cast expression

struct B -> { a: i64, b: i32 }

fn main() -> void {

    let a = 12 as bool;

    let b = true as B;

    let c = 'a' as i32*;


}
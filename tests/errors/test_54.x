// Invalid LHS assignment

fn foo() -> void {}

fn main() -> i64 {
    let mut x = 21;
    19 = 21;

    foo() = x;
}
// Invalid LHS assignment

fn foo() -> void {}

fn main() -> int {
    let mut x = 21;
    19 = 21;

    foo() = x;
}
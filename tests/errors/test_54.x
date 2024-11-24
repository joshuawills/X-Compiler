// Invalid LHS assignment

fn foo() -> void {}

fn main() -> void {
    let mut x = 21;
    19 = 21;

    foo() = x;
}
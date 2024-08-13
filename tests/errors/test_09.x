// Attempt to use a function as a scalar

fn foo() -> int { return 0; }

fn main() -> int {
    let x: int = foo;
}
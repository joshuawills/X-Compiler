// Attempt to use a function as a scalar

fn foo() -> i64 { return 0; }

fn main() -> i64 {
    let x: i64 = foo;
}
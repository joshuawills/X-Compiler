// Attempt to use a scalar/function as an array

fn foo() -> void {

}

fn main() -> int {

    foo[0] = 19;

    let mut x: int = 21;

    x[2] = 19;

}
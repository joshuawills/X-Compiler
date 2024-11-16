// Attempt to use a scalar/function as an array

fn foo() -> void {

}

fn main() -> i64 {

    foo[0] = 19;

    let mut x: i64 = 21;

    x[2] = 19;

}
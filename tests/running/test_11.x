// Structs and functions!

struct Data -> {
    x: i64,
    y: i64
}

fn foo(x: Data) -> void {}

fn fooP(x: Data*) -> void {}

fn main() -> void {

    let mut y = Data { 2, 3 };
    foo(y);
    fooP(&y);

}
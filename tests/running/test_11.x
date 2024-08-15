// Structs and functions!

struct Data -> {
    x: int,
    y: int
}

fn foo(x: Data) -> void {}

fn fooP(x: Data*) -> void {}

fn main() -> int {

    let mut y = Data { 2, 3 };
    foo(y);
    fooP(&y);

}
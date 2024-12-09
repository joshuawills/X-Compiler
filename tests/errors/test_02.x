// Identifier redeclared

let x: i64 = 21;
let x: i64 = 21;

struct Foo -> {
    x: i64
}

fn (v: Foo*) foo(v: Foo) -> void {}

fn foo(a: i64, a: i8) -> void {}

fn main() -> void {}

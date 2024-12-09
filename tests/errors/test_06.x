// Incompatible type for return

struct Foo -> {
    x: i64
}

fn (v: Foo*) foo() -> void {
    return 21;
}


fn foo() -> void {
    return 21;
}

fn main() -> void {
    return 1;
}
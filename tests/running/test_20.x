// Nested struct accesses with primitive types

import std, io;

struct IntBox -> {
    val: i64
}

struct BoxOfMany -> {
    val2: IntBox,
    val3: bool
}

fn main() -> void {

    let a = IntBox { 3 };
    let b = BoxOfMany { a, 2 == 3 };

    io::println(b.val2.val);

}
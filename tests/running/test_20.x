// Nested struct accesses with primitive types

struct IntBox -> {
    val: i64
}

struct BoxOfMany -> {
    val2: IntBox,
    val3: bool
}

fn main() -> i64 {

    let a = IntBox { 3 };
    let b = BoxOfMany { a, 2 == 3 };

    outI64(b.val2.val);

}
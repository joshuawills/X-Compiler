// Nested struct accesses with primitive types

struct IntBox -> {
    val: int
}

struct BoxOfMany -> {
    val2: IntBox,
    val3: bool
}

fn main() -> int {

    let a = IntBox { 3 };
    let b = BoxOfMany { a, 2 == 3 };

    outInt(b.val2.val);

}
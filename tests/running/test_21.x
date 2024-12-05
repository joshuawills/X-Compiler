// declaring sub structs in place

import "../../lib/std.x" as std;

struct IntBox -> {
	val: i64
}

struct BasicStruct -> {
    x: IntBox,
    y: i64
}

struct IntBox2 -> {
    val: i64,
    val2: i64
}

struct BasicStruct2 -> {
    x: IntBox2,
    y: i64
}

fn main() -> void {

    let y = BasicStruct { IntBox { 2 }, 3 };

    let z = BasicStruct2 { IntBox2 { 2, 3 }, 4};

	std::println(y.x.val);
	std::println(z.x.val2);
}

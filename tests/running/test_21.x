// declaring sub structs in place

import std, io;

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

	io::println(y.x.val);
	io::println(z.x.val2);
}

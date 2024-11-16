// declaring sub structs in place

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

fn main() -> i64 {

    let y = BasicStruct { IntBox { 2 }, 3 };

    let z = BasicStruct2 { IntBox2 { 2, 3 }, 4};

	outI64(y.x.val);
	outI64(z.x.val2);
}

// declaring sub structs in place

struct IntBox -> {
	val: int
}

struct BasicStruct -> {
    x: IntBox,
    y: int
}

struct IntBox2 -> {
    val: int,
    val2: int
}

struct BasicStruct2 -> {
    x: IntBox2,
    y: int
}

fn main() -> int {

    let y = BasicStruct { IntBox { 2 }, 3 };

    let z = BasicStruct2 { IntBox2 { 2, 3 }, 4};

	outInt(y.x.val);
	outInt(z.x.val2);
}

// declaring sub structs in place

struct IntBox -> {
	val: int
}

struct BasicStruct -> {
    x: IntBox,
    y: int
}

fn main() -> int {

    let y = BasicStruct { IntBox { 2 }, 3 };

	outInt(y.x.val);
}

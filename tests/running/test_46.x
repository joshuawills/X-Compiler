// Basic struct return from function

import "../../lib/std.x" as std;

struct IntBox -> { val: i64 }

fn getIntBox(val: i64) -> IntBox {
	let x = IntBox { val };
	return x;
}

fn main() -> void {

	let x: IntBox = getIntBox(21);
	std::println(x.val);

}

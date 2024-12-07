// Basic struct return from function

import std, io;

struct IntBox -> { val: i64 }

fn getIntBox(val: i64) -> IntBox {
	let x = IntBox { val };
	return x;
}

fn main() -> void {

	let x: IntBox = getIntBox(21);
	io::println(x.val);

}

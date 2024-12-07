// Basic tuple return from function

import std;

fn getNumber(val: i64) -> (i64, bool) {

	if val <= 0 {
		let a = (0, false);
		return a;
	}

	let b = (val, true);
	return b;

}

fn main() -> void {
	let mut v = getNumber(10);

	if v.1 {
		std::print("You provided a positive number: ");
		std::println(v.0);
	}

	v = getNumber(-1);

	if !v.1 {
		std::print("You provided a negative number: ");
		std::println(v.0);
	}

}

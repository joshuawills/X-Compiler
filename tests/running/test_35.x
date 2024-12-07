// Casting with different numeric types

import std;

fn main() -> void {

	let what = 21 as i8;

	let real = 'a' as i64;

	let val = 19 as f32;

	let val2 = 20.0 as i32;

	let hmm = (21, false, "hello world");

	std::print(type(what));
	std::print(type(real));
	std::print(type(val));
	std::print(type(val2));
	std::print(type(hmm));

}

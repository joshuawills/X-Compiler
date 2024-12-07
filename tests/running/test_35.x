// Casting with different numeric types

import std, io;

fn main() -> void {

	let what = 21 as i8;

	let real = 'a' as i64;

	let val = 19 as f32;

	let val2 = 20.0 as i32;

	let hmm = (21, false, "hello world");

	io::print(type(what));
	io::print(type(real));
	io::print(type(val));
	io::print(type(val2));
	io::print(type(hmm));

}

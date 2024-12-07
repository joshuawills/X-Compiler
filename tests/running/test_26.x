// Testing size of

import std, io;

struct A -> { b: i64, c: bool}

fn main() -> void {
	
	let a = A { 2, false };

	io::println(size(char*));
	io::println(size(i32*));

	io::println(size(i64));
	io::println(size(i8));
	io::println(size(i32));

	io::println(size(bool));

	io::println(size(f32));
	io::println(size(f64));

	io::println(size(A));
	io::println(size(a));

	let b = [3, 4, 5];
	io::println(size(b));

	let c = (1, false, 2 as i8);
	io::println(size(c));

	let d = (32, false);
	io::println(size(d));

	let e = (false, A { 3, false });
	io::println(size(e));

}
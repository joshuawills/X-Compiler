// Testing size of

import "../../lib/std.x" as std;

struct A -> { b: i64, c: bool}

fn main() -> void {
	
	let a = A { 2, false };

	std::println(size(char*));
	std::println(size(i32*));

	std::println(size(i64));
	std::println(size(i8));
	std::println(size(i32));

	std::println(size(bool));

	std::println(size(f32));
	std::println(size(f64));

	std::println(size(A));
	std::println(size(a));

	let b = [3, 4, 5];
	std::println(size(b));

	let c = (1, false, 2 as i8);
	std::println(size(c));

	let d = (32, false);
	std::println(size(d));

	let e = (false, A { 3, false });
	std::println(size(e));

}
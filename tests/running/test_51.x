// Basic pointer comparisons

import io;

fn main() -> void {

	let mut x = 21;
	let mut y = 19;

	let mut x_p = &x;
	let mut y_p = &y;

	io::println(x_p > y_p);
	io::println(x_p >= y_p);

	io::println(x_p < y_p);
	io::println(x_p <= y_p);

	io::println(x_p == y_p);
	io::println(x_p != y_p);

	x_p += 1;
}


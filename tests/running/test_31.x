// Floats works now

import std, io;

fn main() -> void {

	let mut x = 0.1;

	while x < 2.0 {
		io::println(x);
		x += 0.1;
	}

	let mut y = 1.0;

	io::println(y * 3 / 2);

}


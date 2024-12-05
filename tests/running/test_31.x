// Floats works now

import "../../lib/std.x" as std;

fn main() -> void {

	let mut x = 0.1;

	while x < 2.0 {
		std::println(x);
		x += 0.1;
	}

	let mut y = 1.0;

	std::println(y * 3 / 2);

}


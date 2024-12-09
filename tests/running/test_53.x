// Can redeclare values in the same scope and override them

import io, std, math;

fn main() -> void {

	let val, err = math::tan(0.0);
	if err.isError {
		io::println(err.message);
	} else {
		io::println(val);
	}

	let val, err = math::tan(math::PI / 4);
	if err.isError {
		io::println(err.message);
	} else {
		io::println(val);
	}
}


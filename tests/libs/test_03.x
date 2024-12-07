// Tan X testing

import std, io, math;

fn main() -> void {

	let res = math::tan(math::PI / 2);
	let err = res.1;
	if err.isError {
		io::print("OHOH\n");
	} else {
		io::println(res.0);
	}

	let res2 = math::tan(math::PI / 4);
	let err2 = res2.1;
	if err2.isError {
		io::print("OHOH\n");
	} else {
		io::println(res2.0);
	}

	let res3 = math::tan(90.0, math::TrigOptions.DEGREES);
	let err3 = res3.1;
	if err3.isError {
		io::print("OHOH\n");
	} else {
		io::println(res3.0);
	}

	let res4 = math::tan(45.0, math::TrigOptions.DEGREES);
	let err4 = res4.1;
	if err4.isError {
		io::print("OHOH\n");
	} else {
		io::println(res4.0);
	}

}
 
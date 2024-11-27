// Basic tuple return from function

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
		outStr("You provided a positive number: ");
		outI64(v.0);
	}

	v = getNumber(-1);

	if !v.1 {
		outStr("You provided a negative number: ");
		outI64(v.0);
	}

}

// Cannot pass immutable data types to mutable method

fn (mut v: i64) square() -> i64 {
	return v * v;
}

fn main() -> void {
	let x = 21;
	let y = x.square();
}

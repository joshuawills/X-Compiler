// Floats works now

fn main() -> void {

	let mut x = 0.1;

	while x < 2.0 {
		outF64(x);
		x += 0.1;
	}

	let mut y = 1.0;

	outF64(y * 3 / 2);

}


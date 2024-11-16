// Floats works now

fn main() -> int {

	let mut x = 0.1;

	while x < 2.0 {
		outFloat(x);
		x += 0.1;
	}

	let mut y = 1.0;

	outFloat(y * 3 / 2);

}


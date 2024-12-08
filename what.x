// Basic pointer comparison

import io;

fn main() -> void {

	let mut x = 21;
	let mut y = &x;
	let mut z = y;

	io::println(y);
	io::println(z);
	if y >= 2 {
		io::println("The two pointers are equal");
	} else {
		io::println("The two pointers are not equal");
	}
		
}

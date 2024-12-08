// Basic pointer comparison and printing :)

import io;

fn main() -> void {

	let mut x = 21;
	let mut y = &x;
	let mut z = y;

	if y == z {
		io::println("The two pointers are equal");
	}
		
}

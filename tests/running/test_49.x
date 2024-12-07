// Messing around with tuple destructuring

import std, io;

struct Data -> {
	mut x: i64
}

fn main() -> void {

	let mut a = ((1, 2), (3, 4));

	let mut b, c = a;

	io::print(type(a));
	io::print(type(b));
	io::print(type(c));

	let mut d, e = b;	

	b.0 = 19;
	d = 21;
	if b.0 != d {
		io::print("It's worked!\n");
	}

	let mut what = (true, Data { 21 });
	let mut _, structure = what;

	structure.x = 19;

	let s = what.1;
	io::println(s.x);
	io::println(structure.x);


}

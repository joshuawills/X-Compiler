// Messing around with tuple destructuring

import std;

struct Data -> {
	mut x: i64
}

fn main() -> void {

	let mut a = ((1, 2), (3, 4));

	let mut b, c = a;

	std::print(type(a));
	std::print(type(b));
	std::print(type(c));

	let mut d, e = b;	

	b.0 = 19;
	d = 21;
	if b.0 != d {
		std::print("It's worked!\n");
	}

	let mut what = (true, Data { 21 });
	let mut _, structure = what;

	structure.x = 19;

	let s = what.1;
	std::println(s.x);
	std::println(structure.x);


}

// Struct values are correctly duplicated

import std, io;

struct A -> {
	mut v: i64
}

fn main() -> void {
	let mut b = A { 2 };
	let mut v = b;
	b.v = 3;
	io::println(b.v);
	io::println(v.v);

	let mut vals = [
		A { 2 },
		A { 3 }
	];

	let mut r = vals[0];
	let mut p = vals[0];

	r.v = 1;
	p.v = 2;

	io::println(r.v);
	io::println(p.v);

	io::println(vals[0].v);
	io::println(vals[1].v);
}
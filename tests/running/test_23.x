// Struct values are correctly duplicated

import "../../lib/std.x" as std;

struct A -> {
	mut v: i64
}

fn main() -> void {
	let mut b = A { 2 };
	let mut v = b;
	b.v = 3;
	std::println(b.v);
	std::println(v.v);

	let mut vals = [
		A { 2 },
		A { 3 }
	];

	let mut r = vals[0];
	let mut p = vals[0];

	r.v = 1;
	p.v = 2;

	std::println(r.v);
	std::println(p.v);

	std::println(vals[0].v);
	std::println(vals[1].v);
}
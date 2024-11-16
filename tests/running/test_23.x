// Struct values are correctly duplicated

struct A -> {
	mut v: i64
}

fn main() -> i64 {
	let mut b = A { 2 };
	let mut v = b;
	b.v = 3;
	outInt(b.v);
	outInt(v.v);

	let mut vals = [
		A { 2 },
		A { 3 }
	];

	let mut r = vals[0];
	let mut p = vals[0];

	r.v = 1;
	p.v = 2;

	outInt(r.v);
	outInt(p.v);
}
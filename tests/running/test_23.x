// Struct values are correctly duplicated

struct A -> {
	mut v: i64
}

fn main() -> i64 {
	let mut b = A { 2 };
	let mut v = b;
	b.v = 3;
	outI64(b.v);
	outI64(v.v);

	let mut vals = [
		A { 2 },
		A { 3 }
	];

	let mut r = vals[0];
	let mut p = vals[0];

	r.v = 1;
	p.v = 2;

	outI64(r.v);
	outI64(p.v);
}
// Messing around with tuple destructuring

struct Data -> {
	mut x: i64
}

fn main() -> void {

	let mut a = ((1, 2), (3, 4));

	let mut b, c = a;

	outStr(type(a));
	outStr(type(b));
	outStr(type(c));

	let mut d, e = b;	

	b.0 = 19;
	d = 21;
	if b.0 != d {
		outStr("It's worked!\n");
	}

	let mut what = (true, Data { 21 });
	let mut _, structure = what;

	structure.x = 19;

	let s = what.1;
	outI64(s.x);
	outI64(structure.x);


}

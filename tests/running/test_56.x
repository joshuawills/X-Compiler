// Basic method accesses, local and library

import io, str, std;

struct Foo -> {
	mut val: i8*
}

fn (v: Foo*) get_val() -> i8* {
	return v->val;
}

fn new_foo() -> Foo* {
	let mut f: Foo* = std::malloc(size(Foo));
	f->val = "hello, world!";
	return f;
}

fn (v: Foo*) free() -> void {
	std::free(v);
}

fn main() -> void {

	let mut v = new_foo();
	let mut c = v.get_val();
	let l = c.len();
	io::println(c);
	io::println(l);
	v.free();

}

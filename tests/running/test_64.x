// Accessing methods defined in an implementation

using io, conv, str, std;

struct Foo -> {
	mut val: i64
}

trait ToString -> {
	*to_string() -> str*;
}

impl ToString for Foo -> {

	fn (v: Foo*) to_string() -> str* {
		let mut val, _ = (v->val).to_str();
		return val;
	}

}

fn Foo(val: i64) -> Foo* {
	let mut foo = malloc(size(Foo)) as Foo*;
	foo->val = val;
	return foo;
}

fn (v: Foo*) free() -> void {
	free(v);
}


fn main() -> void {
	let mut foo = Foo(42);
	let mut s = foo.to_string();
	println(s);
	s.free();
	foo.free();
}

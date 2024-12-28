// Types don't match up in the trait implementation

using io, conv, str;

struct Foo -> {
	val: i64
}

struct Bar -> {
	val: i64
}

trait ToString -> {
	*to_string() -> str*;
}

impl ToString for Foo -> {

	fn (v: Bar*) to_string() -> str* {
		let mut val, _ = (v->val).to_str();
		return val;
	}

}


fn main() -> void {

}

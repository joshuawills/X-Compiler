// No such method on trait

using io, conv, str;

struct Foo -> {
	val: i64
}

trait ToString -> {
	*to_string() -> str*;
}

impl ToString for Foo -> {

	fn (v: Foo*) to_bar() -> str* {
		let mut val, _ = (v->val).to_str();
		return val;
	}

	fn (v: Foo*) to_string() -> str* {
		let mut val, _ = (v->val).to_str();
		return val;
	}

}


fn main() -> void {

}

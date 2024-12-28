// Missing methods for trait implementation

using io, conv, str;

struct Foo -> {
	val: i64
}

trait ToString -> {
	*to_string() -> str*;
}

impl ToString for Foo -> {
}


fn main() -> void {

}

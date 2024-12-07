// Can infer void* cast from type annotation

import std, io;

struct Node -> {
	value: i64
}

fn main() -> void {

	let value: Node* = std::malloc(size(Node));
	io::print(type(value));
	std::free(value);

}

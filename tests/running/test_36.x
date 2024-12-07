// Can infer void* cast from type annotation

import std;

struct Node -> {
	value: i64
}

fn main() -> void {

	let value: Node* = std::malloc(size(Node));
	std::print(type(value));
	std::free(value);

}

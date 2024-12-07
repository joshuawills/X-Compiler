// Typecasting with void pointers

import std;

struct Node -> {
	value: i64
}

fn main() -> void {

	let value: Node* = std::malloc(size(Node));

	std::free(value);

}

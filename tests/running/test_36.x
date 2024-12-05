// Can infer void* cast from type annotation

import "../../lib/std.x" as std;

struct Node -> {
	value: i64
}

fn main() -> void {

	let value: Node* = std::malloc(size(Node));
	std::print(type(value));
	std::free(value);

}

// Can infer void* cast from type annotation

import "../../lib/std.x" as std;

struct Node -> {
	value: i64
}

fn main() -> void {

	let value: Node* = std::malloc(size(Node));
	outStr(type(value));
	std::free(value as void*);

}

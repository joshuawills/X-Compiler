// Typecasting with void pointers

import "../../lib/std.x" as std;

struct Node -> {
	value: i64
}

fn main() -> void {

	let value = std::malloc(size(Node)) as Node*;

	std::free(value as void*);

}

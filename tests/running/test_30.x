// Can use malloc and free

import "../../lib/std.x" as std;

fn main() -> void {

	let x: void* = std::malloc(size(i64));

	let y: void* = std::malloc(8 * size(i8));

	std::free(x);
	std::free(y);

}

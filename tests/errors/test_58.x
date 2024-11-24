// Non existent struct member

struct Box -> {
	a: i64
}

fn main() -> void {
	let b = Box { 2 };
	let c = b.d;
}

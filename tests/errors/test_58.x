// Non existent struct member

struct Box -> {
	a: int
}

fn main() -> int {
	let b = Box { 2 };
	let c = b.d;
}

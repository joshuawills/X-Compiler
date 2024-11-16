// Non existent struct member

struct Box -> {
	a: i64
}

fn main() -> i64 {
	let b = Box { 2 };
	let c = b.d;
}

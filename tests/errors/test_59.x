// Subtype is not a struct type

struct A -> {
	B: bool
}

fn main() -> i64 {

	let var = A { false };

	let z  = var.B.C;

}


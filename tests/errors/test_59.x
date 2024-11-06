// Subtype is not a struct type

struct A -> {
	B: bool
}

fn main() -> int {

	let var = A { false };

	let z  = var.B.C;

}


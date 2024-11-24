// Subtype is not a struct type

struct A -> {
	B: bool
}

fn main() -> void {

	let var = A { false };

	let z  = var.B.C;

}


// Testing size of

struct A -> { b: int, c: bool}

fn main() -> int {
	
	let a = A { 2, false };

	outInt(size(char*));
	outInt(size(int));
	outInt(size(bool));
	outInt(size(float));

	outInt(size(A));
	outInt(size(a));

	outInt(size(A[10]));
	outInt(size(bool[5]));

	let b = [3, 4, 5];
	outInt(size(b));
}
// Testing size of

struct A -> { b: i64, c: bool}

fn main() -> i64 {
	
	let a = A { 2, false };

	outI64(size(char*));
	outI64(size(i64));
	outI64(size(bool));
	outI64(size(f32));

	outI64(size(A));
	outI64(size(a));

	outI64(size(A[10]));
	outI64(size(bool[5]));

	let b = [3, 4, 5];
	outI64(size(b));
}
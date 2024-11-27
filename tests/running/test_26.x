// Testing size of

struct A -> { b: i64, c: bool}

fn main() -> void {
	
	let a = A { 2, false };

	outI64(size(char*));
	outI64(size(i32*));

	outI64(size(i64));
	outI64(size(i8));
	outI64(size(i32));

	outI64(size(bool));

	outI64(size(f32));
	outI64(size(f64));

	outI64(size(A));
	outI64(size(a));

	let b = [3, 4, 5];
	outI64(size(b));

	let c = (1, false, 2 as i8);
	outI64(size(c));

	let d = (32, false);
	outI64(size(d));

	let e = (false, A { 3, false });
	outI64(size(e));

}
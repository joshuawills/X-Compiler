// Can use malloc and free

fn main() -> i64 {

	let x: i64* = malloc(size(i64));

	let y: i8* = malloc(8 * size(i8));

	free(x);
	free(y);

}

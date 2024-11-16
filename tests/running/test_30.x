// Can use malloc and free

fn main() -> i64 {

	let x: i64* = malloc(size(i64));

	let y: char* = malloc(8 * size(char));

	free(x);
	free(y);

}

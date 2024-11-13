// Can use malloc and free

fn main() -> int {

	let x: int* = malloc(size(int));

	let y: char* = malloc(8 * size(char));

	free(x);
	free(y);

}

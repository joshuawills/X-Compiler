// Working with min and max unsigned int values

using io, std;

fn main() -> void {

	let mut x = U8_MIN;
	println(type(x));
	x -= 1;
	println(x);
	println(x == U8_MAX);

	let mut y = U32_MIN;
	println(type(y));
	y -= 1;
	println(y);
	println(y == U32_MAX);

	let mut z = U64_MIN;
	println(type(z));
	z -= 1;
	println(z);
	println(z == U64_MAX);

}

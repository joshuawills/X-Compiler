// Basic string to i64 test

using conv, io, str;

fn main() -> void {

	let x, err = "    21".to_i64();
	if err.isError {
		println("ohoh");
	}
	println(x);

	let y, err = "foo".to_i64();
	if err.isError {
		println("ohoh");
	}
	println(y);

	let mut x, err = (21).to_str(); 
	println(x);
	x.free();

	let mut x, err = (0).to_str(); 
	println(x);
	x.free();

	let mut x, err = (-19).to_str(); 
	println(x);
	x.free();
	
}

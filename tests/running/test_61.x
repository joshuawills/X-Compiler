// Testing the using keyword

using io, str, std;

fn main() -> void {
	let mut my_str, _ = Str("hello, world!");
	println(my_str);
	my_str.push(" my name is: ");
	println(my_str);
	my_str.push("jane doe");
	println(my_str);
	my_str.free();
	exit(0);
}

// Testing the 'using' stmt

using str, io, math;

fn main() -> void {

	let mut my_str, _ = Str("hello, world!");
	println(my_str);
	my_str.free();

	println(21);
	println("foo");

	println(sin(PI));

}

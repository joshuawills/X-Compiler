// Messing around with methods on direct expressions/built in types and chaining them

import io, std, math, str;

fn (v: i64) square() -> i64 {
	return v * v;
}

fn (v: bool) negate() -> bool {
	return !v;
}

fn main() -> void {

	io::println((2).square());
	io::println("".len());
	io::println("hello, world!".len());
	io::println("hello, world!".len().square());

	io::println(true.negate());
	io::println(false.negate());
	io::println(("".len() == 0).negate());

}

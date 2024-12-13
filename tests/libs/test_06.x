// Testing str len, str equal

import io, str;

fn main() -> void {

	let mut v = "";
	io::println(v.len());
	v = "a";
	io::println(v.len());
	v = "\th";
	io::println(v.len());
	v = "abkajklsjdakjd";
	io::println(v.len());

	v = "";
	io::println(v.equal(""));
	v = "a";
	io::println(v.equal(""));
	v = "";
	io::println(v.equal("b"));
	v = "a";
	io::println(v.equal("b"));
	v = "b";
	io::println(v.equal("b"));
	v = "\t";
	io::println(v.equal("\t"));
	v = "hello world";
	io::println(v.equal("hello world"));
	v = "hello world!";
	io::println(v.equal("hello world"));

}

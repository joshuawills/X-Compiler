// Testing str len, str equal

import io, str;

fn main() -> void {

	io::println(str::str_len(""));
	io::println(str::str_len("a"));
	io::println(str::str_len("\th"));
	io::println(str::str_len("abkajklsjdakjd"));

	io::println(str::str_equal("", ""));
	io::println(str::str_equal("a", ""));
	io::println(str::str_equal("", "b"));
	io::println(str::str_equal("a", "b"));
	io::println(str::str_equal("b", "b"));
	io::println(str::str_equal("\t", "\t"));
	io::println(str::str_equal("hello world", "hello world"));
	io::println(str::str_equal("hello world!", "hello world"));

}

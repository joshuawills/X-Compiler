// Str lib tests more

import io, str;

fn main() -> void {

	let mut c = "Hello, world!";
	io::println(c.contains("world"));
	io::println(c.contains(""));
	io::println(c.contains("bob"));

	let mut my_str, err = str::new_str("Hello, world!");
	str::println(my_str);
	io::println(my_str.empty());

	let mut empty_str, _ = str::new_str("");
	str::println(empty_str);
	io::println(empty_str.empty());

	io::println(my_str.contains(empty_str));
	io::println(my_str.contains("world"));

	my_str.free();
	empty_str.free();
}

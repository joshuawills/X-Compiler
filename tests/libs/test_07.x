// Str lib tests more

import io, str;

fn main() -> void {

	io::println(str::str_contains_substring("Hello, world!", "world"));
	io::println(str::str_contains_substring("Hello, world!", ""));
	io::println(str::str_contains_substring("Hello, world!", "bob"));


	let mut my_str, _ = str::new_str("Hello, world!");
	str::println(my_str);
	io::println(str::string_empty(my_str));

	let mut empty_str, _ = str::new_str("");
	str::println(empty_str);
	io::println(str::string_empty(empty_str));

	io::println(str::string_contains(my_str, empty_str));
	io::println(str::string_contains(my_str, "world"));


	str::str_free(my_str);
	str::str_free(empty_str);
}

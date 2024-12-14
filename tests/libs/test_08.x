// Messing around with methods on direct expressions/built in types and chaining them. Also str library

import io, std, math, str;

fn main() -> void {

	let mut str1, _ = str::new_str("Hello, world!");
	let mut str2, _ = str::new_str("world");

	io::println(str1.len());
	io::println(str2.len());

	io::println(str1.equal(str1));

	io::println(str1.is_alpha());
	io::println("abcd".is_alpha());

	io::println(str1.is_digit());
	io::println("1234".is_digit());

	let mut str3, _ = str::new_str("HELLO");
	io::println(str3.is_upper());
	io::println("HELLO".is_upper());
	
	let mut str4, _ = str::new_str("hello");
	io::println(str4.is_lower());
	io::println("hello".is_lower());
	io::println(" hello".is_lower());

	let err = str4.push(" world");
	if err.isError {
		io::println("oh oh");
	}
	str::println(str4);

	loop 100 {
		let err = str4.push(" world");
		if err.isError {
			io::println("oh oh");
			break;
		}
		str::println(str4);
	}


	str1.free();
	str2.free();
	str3.free();
	str4.free();

}

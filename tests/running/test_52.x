// Number types wrap around correctly

import io, std;

fn main() -> void {

	io::println(std::I8_MIN as i64);
	let val: i8 = std::I8_MIN - 1; 
	io::println(val as i64);

	io::println(std::I8_MAX as i64);
	let val2: i8 = std::I8_MAX + 1;
	io::println(val2 as i64);

	io::println(std::I32_MIN as i64);
	let val3: i32 = std::I32_MIN - 1;
	io::println(val3 as i64);

	io::println(std::I32_MAX as i64);
	let val4: i32 = std::I32_MAX + 1;
	io::println(val4 as i64);

	io::println(std::I64_MIN);
	let val5: i64 = std::I64_MIN - 1;
	io::println(val5);

	io::println(std::I64_MAX);
	let val6: i64 = std::I64_MAX + 1;
	io::println(val6);

}

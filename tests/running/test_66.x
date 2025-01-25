// Basic bitwise operators

using io, std;

fn main() -> void {
	loop i in 63 {
		println(1 << i);
	}

	println("===");

	let v = 1 << 62;
	loop i in 63 {
		println(v >> i);
	}

	println("===");

	println(~1);
	println(~I64_MAX);

	println("===");

	println(U64_MAX & (1 as u64));
	println(U64_MAX | (1 as u64));
}

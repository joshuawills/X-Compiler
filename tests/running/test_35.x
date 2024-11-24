// Casting with different numeric types

fn main() -> void {

	let what = 21 as i8;

	let real = 'a' as i64;

	let val = 19 as f32;

	let val2 = 20.0 as i32;

	outStr(type(what));
	outStr(type(real));
	outStr(type(val));
	outStr(type(val2));

}

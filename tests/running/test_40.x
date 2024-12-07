// No duplicate br/return stmts

import std, io;

fn getBooleanString(val: bool) -> i8* {
	if val {
		io::println(21);
		return "true\n";
	}
	return "false\n";
}

fn main() -> void {

	let a = true;
	io::print(getBooleanString(a));
	io::print(getBooleanString(!a));

}

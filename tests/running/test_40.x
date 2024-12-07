// No duplicate br/return stmts

import std;

fn getBooleanString(val: bool) -> i8* {
	if val {
		std::println(21);
		return "true\n";
	}
	return "false\n";
}

fn main() -> void {

	let a = true;
	std::print(getBooleanString(a));
	std::print(getBooleanString(!a));

}

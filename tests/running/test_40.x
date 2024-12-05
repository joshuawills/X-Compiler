// No duplicate br/return stmts

import "../../lib/std.x" as std;

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

// No duplicate br/return stmts

fn getBooleanString(val: bool) -> i8* {
	if val {
		outI64(21);
		return "true\n";
	}
	return "false\n";
}

fn main() -> i64 {

	let a = true;
	outStr(getBooleanString(a));
	outStr(getBooleanString(!a));

}

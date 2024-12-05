import "lib/std.x" as std;
import "lib/math.x" as math;

fn main() -> void {

	let val, err = math::tan(math::PI / 2);
	
	if err.isError {
		outStr("Error occured\n");
		std::exit(1);
	}

	outStr("tan(PI / 2) = ");
	outF64(val);
	std::exit(0);
}

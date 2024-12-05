import "lib/std.x" as std;
import "lib/math.x" as math;

fn main() -> void {

	let val, err = math::tan(math::PI / 2);
	
	if err.isError {
		std::println("Error occured");
		std::exit(1);
	}

	std::print("tan(PI / 2) = ");
	std::println(val);
	std::exit(0);
}

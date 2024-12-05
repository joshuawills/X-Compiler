// Fibonacci iterative

import "../../lib/std.x" as std;

fn main() -> void {
    let x: i64 = 9;
	std::println(fib(x));
}

fn fib(x: i64) -> i64 {
    let mut f1: i64 = 0;
    let mut f2: i64 = 1;
    let mut fi: i64;
	if x == 0 || x == 1 { return x; }
	loop i in 2 x {
		fi = f1 + f2;
		f1 = f2;
		f2 = fi;
	}
	return fi;
}
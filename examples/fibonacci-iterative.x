fn main() -> int {
	mut int x;
	inInt("Enter number: ", x);
	outInt(fib(x));
}

fn fib(int x) -> int {
	mut int f1 = 0, f2 = 1, fi;
	if x == 0 || x == 1 { return x; }
	loop i in 2 x {
		fi = f1 + f2;
		f1 = f2;
		f2 = fi;
	}
	return fi;
}
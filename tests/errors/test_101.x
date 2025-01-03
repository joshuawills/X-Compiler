// Redeclaration of generic function

fn id<T>(v: T) -> T {
	return v;
}

fn id<G>(v: G) -> G {
	return v;
}

fn main() -> void {
}

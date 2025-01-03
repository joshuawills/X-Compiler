// Duplicate generic types with same name

fn id<T, T>(v: T) -> T {
	return v;
}

fn main() -> void {
}

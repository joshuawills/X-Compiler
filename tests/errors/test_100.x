// No such trait to specify as a generic bound

fn id<T: Foo>(v: T) -> T {
	return v;
}

fn main() -> void {
}

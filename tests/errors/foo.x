
using io;

fn say_hello() -> void {
	println("hello");
}

export fn say_bye() -> void {
	println("bye");
}

export fn id<T>(v: T) -> T {
	return v;
}

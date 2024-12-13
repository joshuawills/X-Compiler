---
title: "Methods"
weight: 1
---

Methods are an experimental feature currently being developed. Methods are 
similar to functions, but may be directly mapped to a type, either primitive
or composite. This allows for far cleaner and easier-to-read code. Below is a
simple example of how to define and utilise methods with a custom type. In this
case, the "len()" method is defined in the str library for i8* types.

```Rust
import io, str, std;

struct Foo -> {
	mut val: i8*
}

fn (v: Foo*) get_val() -> i8* {
	return v->val;
}

fn new_foo() -> Foo* {
	let mut f: Foo* = std::malloc(size(Foo));
	f->val = "hello, world!";
	return f;
}

fn (v: Foo*) free() -> void {
	std::free(v);
}

fn main() -> void {

	let mut v = new_foo();
	io::println(v.get_val().len());
	v.free();

}
```
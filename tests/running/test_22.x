// Can succesfully instantiate an array of structs

import std, io;

struct A -> {
	v: i64
}

struct B -> {
	x: i64[10]
}

fn main() -> void {

	let a: A[2] = [
		A { 2 },
		A { 3 }
	];

	let b = [ A { 2 + 9 - 3} ];

	io::println(a[0].v);
	io::println(a[1].v);
	io::println(b[0].v);

	let val = B { [1, 2, 3, 4, 5, 6, 7, 8, 9, 10] };

	loop i in 10 {
		io::println(val.x[i]);
	}

}


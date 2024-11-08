// Can succesfully instantiate an array of structs

struct A -> {
	v: int
}

fn main() -> int {

	let a: A[2] = [
		A { 2 },
		A { 3 }
	];

    outInt(1);

	let b = [ A { 2 + 9 - 3} ];

}


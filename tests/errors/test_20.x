// Attempting to redeclare a constant

let y: i64[] = [1, 2, 3, 4, 5];

struct A -> {
    mut x: i64
}

fn main() -> void {

    y[0] = 21;

    let x: i64 = 21;

    x += 1;

    let b = A { 2 };

    b.x = 2;

    let c = (21, false);

    c.0 = 19;

	let d, e = (19, 20);

	d += 1;
	e += 1;


}

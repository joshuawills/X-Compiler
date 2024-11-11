// Attempting to redeclare a constant

let y: int[] = [1, 2, 3, 4, 5];

struct A -> {
    mut x: int
}

fn main() -> int {

    y[0] = 21;

    let x: int = 21;

    x += 1;

    let b = A { 2 };

    b.x = 2;

}
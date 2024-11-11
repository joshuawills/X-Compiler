// Invalid struct accesses on immutable fields

struct A -> {
    x: int 
}

struct B -> {
    mut c: A
}

fn main() -> int {

    let mut val = A { 3 };

    val.x += 1;

    let mut val2 = B { A { 1 } };

    val2.c.x = 3;


}
// Invalid struct accesses on immutable fields

struct A -> {
    x: i64 
}

struct B -> {
    mut c: A
}

fn main() -> i64 {

    let mut val = A { 3 };

    val.x += 1;

    let mut val2 = B { A { 1 } };

    val2.c.x = 3;


}
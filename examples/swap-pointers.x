fn swap(mut a: int*, mut b: int*) -> void {
    let temp: int = *a;
    *a = *b;
    *b = temp;
}

fn main() -> int {
    let mut a: int = 2;
    let mut b: int = 3;
    swap(&a, &b);
    outInt(a);
    outInt(b);
    return 0;
}
// Swap POINTERS

fn swap(mut a: i64*, mut b: i64*) -> void {
    let temp: i64 = *a;
    *a = *b;
    *b = temp;
}

fn main() -> i64 {
    let mut a: i64 = 2;
    let mut b: i64 = 3;
    swap(&a, &b);
    outInt(a);
    outInt(b);
    return 0;
}
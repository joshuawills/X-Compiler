fn swap(mut int *a, mut int *b) -> void {
    int temp = *a;
    *a = *b;
    *b = temp;
}

fn main() -> int {
    mut int a = 2, b = 3;
    swap(&a, &b);
    outInt(a);
    outInt(b);
    return 0;
}
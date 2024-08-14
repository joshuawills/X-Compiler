---
title: 'Swap Pointers'
weight: 1
---

```Rust
fn swap(mut a: int*, mut b: int*) -> void {
    let temp = *a;
    *a = *b;
    *b = temp;
}

fn main() -> int {
    let mut a = 2;
    let mut b = 3;
    swap(&a, &b);
    outInt(a);
    outInt(b);
    return 0;
}
```
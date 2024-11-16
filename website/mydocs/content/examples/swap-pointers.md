---
title: 'Swap Pointers'
weight: 1
---

```Rust
fn swap(mut a: i64*, mut b: i64*) -> void {
    let temp = *a;
    *a = *b;
    *b = temp;
}

fn main() -> i64 {
    let mut a = 2;
    let mut b = 3;
    swap(&a, &b);
    outI64(a);
    outI64(b);
    return 0;
}
```
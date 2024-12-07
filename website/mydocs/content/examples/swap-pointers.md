---
title: 'Swap Pointers'
weight: 1
---

```Rust
import std;

fn swap(mut a: i64*, mut b: i64*) -> void {
    let temp = *a;
    *a = *b;
    *b = temp;
}

fn main() -> void {
    let mut a = 2;
    let mut b = 3;
    swap(&a, &b);
    std::println(a);
    std::println(b);
}
```
---
title: 'Function Overloading'
weight: 1
---

```Rust
fn add(x: i64) -> i64 {
    return add(0, x);
}

fn add(x: i64, y: i64) -> i64 {
    return x + y;
}

fn main() -> void {
    io::println(add(2, 1)); // logs 3
    io::println(add(2));   // logs 2
}
```
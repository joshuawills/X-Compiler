---
title:  'Euclidean Algorithm'
weight: 1
---

```Rust
fn main() -> void {

    let x = 6;
    let y = 2;

    io::println(euclid(x, y));
    return;
}

fn euclid(x: i64, y: i64) -> i64 {
    if x == 0 {
        return y;
    }
    return euclid(y % x, x);
}
```
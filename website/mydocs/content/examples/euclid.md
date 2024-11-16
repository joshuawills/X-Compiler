---
title:  'Euclidean Algorithm'
weight: 1
---

```Rust
fn main() -> i64 {

    let x = 6;
    let y = 2;

    outI64(euclid(x, y));
    return 0;
}

fn euclid(x: i64, y: i64) -> i64 {
    if x == 0 {
        return y;
    }
    return euclid(y % x, x);
}
```
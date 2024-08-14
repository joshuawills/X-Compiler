---
title:  'Euclidean Algorithm'
weight: 1
---

```Rust
fn main() -> int {

    let x = 6;
    let y = 2;

    outInt(euclid(x, y));
    return 0;
}

fn euclid(x: int, y: int) -> int {
    if x == 0 {
        return y;
    }
    return euclid(y % x, x);
}
```
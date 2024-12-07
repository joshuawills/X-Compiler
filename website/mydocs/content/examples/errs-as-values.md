---
title: 'Errors as Values'
weight: 1
---

```Rust
import math, math;

fn main() -> void {

    let val, err = math::tan(math::PI / 2);

    if err.isError {
        io::println("Error occured");
        std::exit(1);
    }

    io::print("tan(PI / 2) = ");
    io::println(val);
    std::exit(0);
}

```
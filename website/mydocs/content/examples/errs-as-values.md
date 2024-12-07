---
title: 'Errors as Values'
weight: 1
---

```Rust
import std;
import math;

fn main() -> void {

    let val, err = math::tan(math::PI / 2);

    if err.isError {
        std::println("Error occured");
        std::exit(1);
    }

    std::print("tan(PI / 2) = ");
    std::println(val);
    std::exit(0);
}

```
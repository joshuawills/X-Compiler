---
title: 'Fibonacci Sequence'
weight: 1
---

```Rust
fn main() -> int {
    let x = 9;
	
    outInt(fib(x));
}


fn fib(x: int) -> int {
    let mut f1 = 0;
    let mut f2 = 1;
    let mut fi: int;
	
    if x == 0 || x == 1 { 
        return x; 
    }
	
    loop i in 2 x {
	fi = f1 + f2;
	f1 = f2;
	f2 = fi;
    }

    return fi;
}
```
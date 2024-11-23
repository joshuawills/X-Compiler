---
title: "Types"
weight: 1
---

At the moment, X supports several different base types. They are `i64`, `i32`, `i8`, `f32`, `f64` and `bool`.
Implicit type casting will occur from integers to floats, and between integers and characters. Any 
other attempt to assign one type to another will currently fail. Statically-sized arrays are also supported, as well as enums. Some examples of these types is shown below.

```Rust
enum DaysOfWeek -> {
    MON, TUE, WED, THU, FRI, SAT, SUN
}

fn main() -> i64 {
    let x: i64 = 21;
    
    let y: bool = true && false; // true and false are reserved keywords in X
    
    let mut z: i8= 'c';
    char += 2; // implicit type casting occurs here

    let z: float = 19; // here a type annotation is required, as the RHS is identified as an integer

    let arr: i64[] = [1, 2, 3, 4, 5, 6]; // the int[] is not necessary, as is the case for primitive types also

    let mut day = DaysOfWeek.TUE;
    day *= 2; // enums are just ints under the hood, so implicit type casting occurs here
}
```

### Explicit Type Casting

Explicit type casting can be useful, especially when working with libC methods. Below are some examples of how to utilise explicit type casting using the **as** keyword.

```Rust
import "lib/std.x" as std;

struct Node -> {
    val: i64
}

fn main() -> int {

    // The 'as' negates the need for a type signature
    let myNode = std::malloc(size(Node)) as Node*;

    // Alternatively
    let myNode2: Node* = std::malloc(size(Node));

    // As integer expressions default to i64, the explicit casting removes the need for 
    // a type annotation
    let myNum = 21 as i8;

    // Alternatively
    let myNum2: i8 = 21;

}

```


### Structs

Struct types are an experimental feature currently being developed for the X-Compiler. They can be nested,
and are accessed using a dot notation. Use the *->* syntax, like C, for pointer accesses. An example is below.

```Rust
struct BoolBox -> {
    cond: bool
}

struct Data -> {
    vals: BoolBox,
    num: i64
}

fn main() -> i64 {
    let a = Data {
        BoolBox { 
            false
        },
        2
    };

    if !a.vals.cond {
        outI64(a.num);
    }
}

```
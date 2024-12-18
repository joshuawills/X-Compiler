---
title: "Types"
weight: 1
---

At the moment, X supports several different base types. They are `i64`, `i32`, `i8`, `u64`, `u32`, `u8`, `f32`, `f64` and `bool`.
Implicit type casting will occur from integers to floats, and between integers and characters. Any 
other attempt to assign one type to another will currently fail. Statically-sized arrays are also supported, as well as enums. Some examples of these types is shown below.

You also have pointer types, specified by appending an '*' to the end of a type.

```Rust
enum DaysOfWeek -> {
    MON, TUE, WED, THU, FRI, SAT, SUN
}

fn main() -> void {
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
import std, io;

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

fn main() -> void {
    let a = Data {
        BoolBox { 
            false
        },
        2
    };

    if !a.vals.cond {
        io::println(a.num);
    }
}

```

### Tuples

Tuple types operate in much the same way they do in other languages. A tuple type is a collection of values, potentially of 
different types. At a low level, a tuple gets treated in the same way a struct does. These types are useful to return potential error values 
from a function. Elements from a tuple can be accessed using a period *.* and an integer following. This value will be checked to ensure it's in
bounds, and is zero-indexed.

```Rust
fn main() -> void {

    let myTuple = (21, true, "hello, world!\n");

    if myTuple.1 {
        io::println(myTuple.2);
    }

    myTuple.0 += 1; // cause a compilation error as the tuple is defined as immutable
}
```

A tuple variable may be destructured. This use case is particularly valuable when making a function call that may fail, where the failure will be embodied in the error type. For non primitive types, the value
is copied, not passed by reference, in this process.

```Rust
fn main() -> void {

    let val, err = functionThatMayFail();
    if err.isError {
        io::println("handle error");
    }
    io::println("confident errors have been handled");

}
```
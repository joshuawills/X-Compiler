---
title: "Variables"
weight: 1
---

Variables are declared using the `let` keyword. All variables are constant by default, so mutability
is specified with the `mut` keyword, that follows `let`. This keyword can also be used in the context
of function parameter definitions. Type annotations are also a feature of the language, though are not
recommended unless necessary for simplicity of reading, as the compiler can deduce it. The exception
is when you declare a variable without assigning an initial expression to it, or when declaring function
parameters.

```Rust
let x: i64 = 21; // the type annotation here is not necessary, as its deducable from the RHS

let mut x: i64 = 21; // this variable can be mutated later on in the programs runtime
```

X supports both global and local variables. They are assigned in the same way. 

Basic pointer types are also currently supported in X. These are experimental however, and may have
problems.

```Rust
fn swap(mut a: i64*, mut b: i64*) -> void {
    let temp= *a;
    *a = *b;
    *b = temp;
}

fn main() -> void {
    let mut a = 2;
    let mut b = 3;
    swap(&a, &b);
    outI64(a);
    outI64(b);
    return;
}
```
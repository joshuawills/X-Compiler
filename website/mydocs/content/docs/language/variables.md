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

Global variable names **must** be unique. However, for local variables, they do not need to be, but the compiler
will complain in some instances. It is recommended that variable declarations are overwritten only in the case of
tuple destructuring or when specifying a variable as unused, i.e. '_'.


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
    io::println(a);
    io::println(b);
    return;
}
```
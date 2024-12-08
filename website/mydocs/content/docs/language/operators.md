--- 
title: "Operators"
weight: 1
---

X supports all standard mathematical operators a language like C has. For a formal understanding of operator
precedence, check out the [grammar page](/docs/grammar), but it can be summarised as below. All expressions are left-associative.

1. 
    - unary negation and positive (*-* and *+*) for integers
    - unary negation (*!*) for booleans
    - function calls
    - literal values (e.g. *true*, *12*)
    - bracketed expressions
2. 
    - modulo *%*
    - multiplication *\**
    - division */*
3. 
    - addition *+*
    - subtraction *-*
4.
    - less than, less than or equal *<* *<=*
    - greater than, greater than or equal *>* *>=*
5. 
    - equals *==*
    - not equals *!=*
6.
    - and-logic *&&*
    - or-logic *||*

X supports some shorthand syntax for redeclaring numeric types

```Rust
fn main() -> void {
    let mut x = 21;
    x += 21; // '+=', '-=', '*=' and '/=' are all supported
}
```

Note X does **not** support the postfix and prefix operators `++` and `--`. This was an intentional 
choice as such operators are not necessary and can cause confusion.

## Pointer Arithmetic

X supports basic pointer arithmetic where a pointer value can be added to or subtracted from.
No error checking will happen regarding bounds in pointer arithmetic, so use this feature carefully.
An example of pointer arithmetic can be shown below in the `str_len` method found in the `str` library.

```Rust
fn str_len(mut v: i8*) -> i64 {
    let mut s: i8*;
    let mut c = 0;
    for s = v; *s != '\0'; s += 1 {
        c += 1;
    }
    return c;
}
```
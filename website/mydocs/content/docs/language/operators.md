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
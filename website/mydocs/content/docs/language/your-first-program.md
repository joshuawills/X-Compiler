---
title: "Your First Program"
---

```Rust
fn main() -> int {
    let val = "hello, world\n";
    outStr(val);
    return 0;
}
```

From this block of code, we can already deduce a lot about the general syntax of the language. X
is heavily inspired by the syntax of both Rust and C.

## Program Composition

A program is composed of global variables, functions and type definitions. The definition of global
variables can be seen in the below variables section. Functions are declared as seen below

```Rust
fn foo(x: int, mut y: char*) -> void {
    *y += 1;
    outInt(x);
}
```

The function name follows the `fn` keyword, and function parameters fit in the scope of round brackets
and are comma-separated. Just like-variables, function parameters that are mutated in the scope of the
function must be specified with the `mut` keyword, as is the case for the `y` variable in the above example.
Type annotations are **required** for function parameters. The function return type is then defined following
the `->` arrow.

X supports function overloading. Multiple functions can be declared with the same name, as long as they 
have differing input types. An example of this is shown below.

```Rust
fn add(x: int) -> int {
    return add(0, x);
}

fn add(x: int, y: int) -> int {
    return x + y;
}

fn main() -> int {
    outInt(add(2, 1)); // logs 3
    outInt(add(2));   // logs 2
}
```

The only type definition currently supported are `enums`. Below is an example of how to declare one.

```Rust
enum DaysOfWeek -> {
    MON, TUE, WED, THU, FRI, SAT, SUN
} 
```

## Variables

Variables are declared using the `let` keyword. All variables are constant by default, so mutability
is specified with the `mut` keyword, that follows `let`. This keyword can also be used in the context
of function parameter definitions. Type annotations are also a feature of the language, though are not
recommended unless necessary for simplicity of reading, as the compiler can deduce it. The exception
is when you declare a variable without assigning an initial expression to it, or when declaring function
parameters.

```Rust
let x: int = 21; // the type annotation here is not necessary, as its deducable from the RHS

let mut x: int = 21; // this variable can be mutated later on in the programs runtime
```

X supports both global and local variables. They are assigned in the same way. 

Basic pointer types are also currently supported in X. These are experimental however, and may have
problems.

```Rust
fn swap(mut a: int*, mut b: int*) -> void {
    let temp= *a;
    *a = *b;
    *b = temp;
}

fn main() -> int {
    let mut a = 2;
    let mut b = 3;
    swap(&a, &b);
    outInt(a);
    outInt(b);
    return 0;
}
```


### Types

At the moment, X supports several different base types. They are `int`, `float`, `char` and `bool`.
Implicit type casting will occur from integers to floats, and between integers and characters. Any 
other attempt to assign one type to another will currently fail, and explicit type casting is not 
currently supported. Statically-sized arrays are also supported, as well as enums. Some examples of
these types is shown below.

```Rust
enum DaysOfWeek -> {
    MON, TUE, WED, THU, FRI, SAT, SUN
}

fn main() -> int {
    let x: int = 21;
    
    let y: bool = true && false; // true and false are reserved keywords in X
    
    let mut z: char = 'c';
    char += 2; // implicit type casting occurs here

    let z: float = 19; // here a type annotation is required, as the RHS is identified as an integer

    let arr: int[] = [1, 2, 3, 4, 5, 6]; // the int[] is not necessary, as is the case for primitive types also

    let mut day = DaysOfWeek.TUE;
    day *= 2; // enums are just ints under the hood, so implicit type casting occurs here
}
```

### Structs

Struct types are an experimental feature currently being developed for the X-Compiler. They can be nested,
and are accessed using a dot notation. An example is below.

```Rust
struct BoolBox -> {
    cond: bool
}

struct Data -> {
    vals: BoolBox,
    num: int
}

fn main() -> int {
    let a = Data {
        BoolBox { 
            false
        },
        2
    };

    if !a.vals.cond {
        outInt(a.num);
    }
}

```

## Control Flow

X has the standard control flow operators that other languages have, and behave the same semantically.
You have if statements, which can be succeeded by else-if and/or else statements. There is also
while statements, do-while statements, for-loops and, interestingly, loop statements. Conditions
do **not** need to be nested with brackets like they do in C/Java/etc., however the conditions themselves must be surrounded
by curly braces. This language also has the
`break` and `continue` keywords, with standard semantic usage. Note that because X has a native boolean
type, you can't do certain expressions that you can in C, such as

```C
// This is C code

let x: int = 1;
// Invalid in X
if (x) {
    printf("x\n");
}
// Valid in X
if (x != 0) {
    printf("x\n");
}
```

That is because X semantically only accepts expressions that evaluate as booleans to act as the conditionals
for these different statements. This has been decided as a language choice for more expressiveness, as code 
like that above may be ambiguous at times.

Below are some simple examples of using the various control flow statements
```Rust
// Permissible
if true {
    outStr("A\n");    
} else if false {
    outStr("A\n");    
} else {
    outStr("A\n");    
}

// Permissible
let mut i: int = 0;
while i < 10 {
    outInt(i);
    i += 1;
}

// Permissible
let mut i: int = 0;
do {
    outInt(i);
    i += 1;
} while (i < 10);

// Permissible
let mut i: int;
for i = 0; i < 10; i += 1 {
    outInt(i);
}

// Permissible, but not recommended from a style perspective
while (true) {
    outInt(2);
}

// Curly braces must encompass any control flow, even for single statement conditions
// Impermissible
if true
    outStr("A\n");
while false x = x + 1;
```

### Loop Statement

The vast majority of conditional logic can be handled with the `loop` keyword, and is the recommended 
approach where possible. Semantically, the loop keyword has many forms, which can be most easily expressed with examples.

```Rust
// Standard case
loop {
    outStr("This is an infinite loop");

    // The current value of the iteration is default to the reserved "$" keyword
    // This value starts at 0 and goes up once every iteration
    if $ == 10 {
        // Break and continue work as expected with loop
        break;
    }
}

// Upper limit
// This is semantically equivalent to "for $ = 0; $ < 10; $++"
loop 10 {
    outInt($);
}

// Upper and lower limit
// This is semantically equivalent to "for $ = 1; $ < 10; $++"
loop 1 10 {
    outInt($);
}

// Custom variable
// This overrides the $ value to allow for more expressiveness
loop x {
    outInt(x);
}

// This case combines all these features into one example
loop x in 1 10 {
    outInt(x);
}
```

## Operators

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
fn main() -> int {
    let mut x = 21;
    x += 21; // '+=', '-=', '*=' and '/=' are all supported
}
```

Note X does **not** support the postfix and prefix operators `++` and `--`. This was an intentional 
choice as such operators are not necessary and can cause confusion.

## Standard Library

Although quite primitive, we have some basic IO functions as a standard library.

`outInt` takes in an integer and prints it to stdout, followed by a newline.

`outChar` takes in a character and prints it to stdout, followed by a newline.

`outFloat` takes in a float and prints it to stdout, followed by a newline.

`outStr` takes in a string constant and prints it to stdout.
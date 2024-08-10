# X Language Reference

## Hello World Program

```Rust
fn main() -> int {
    outInt(1);
    return 0;
}
```

To run this program, write it to a file called `hello.x` and compile it with the command line
arguments `hello.x -o hello`. The file `hello` will be generated as a binary executable.

## General Structure

An X program is composed of a series of global variable declarations and function declarations.
Variables are declared in a Rust-like manner, including the mutability specifier `mut`.

*Example Variable Declarations*

```Rust
let mut x: int = 1; // can be modified
let isEven: bool = x % 2 == 0; // cannot be modified, checked at compile time
let myName: str = "What is your name? ";
```

Below is an example of a function declaration. The pattern is quite clear, and follows standard C/Rust
like semantics. Note that function parameters are also delimited as being mutable or not.

*Example Function Declaration*

```Rust
fn foo(mut x: int, y: bool) -> void {
    x += 1;
    if x < 10 && y {
        outStr("Condition true");
    }
    return;
}
```

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

## Mutability

I've adopted the Rust approach to variables, that they are constant by default. In order to express
that a variable is mutable, prefix its variable declaration with the keyword `mut`. This keyword
can also be used in the context of function definitions. This is particularly useful from a semantic 
perspective to know if a more advanced variable may be mutated by a function. Note that primitive types
(ints, floats, bools) are passed by value, but anything else is passed by reference (arrays, pointers, etc.).

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

loop x in 1 10 {
    outInt(x);
}
```
## Types

At the moment, the language has a small number of primitive types, and no capacity for complicated
structs. There are boolean expressions, denoted with the `bool` keyword, integers (32-bit signed) with the `int` keyword, 8-bit signed `char`'s and `floats`. Explicit Typecasting is not valid between any of these types currently,
although it is done implicitlty between chars and ints as well as ints to floats.

Simple pointers are also currently supported. They are declared in a C like way, seen in a simple example below.

Statically sized arrays are supported for all types. They are always passed by reference in function calls.

```Rust
fn swap(mut a: int*, mut b: int *) -> void {
    let temp: int = *a;
    *a = *b;
    *b = temp;
}

fn main() -> int {
    let mut a: int = 2;
    let mut b: int = 3;
    swap(&a, &b);
    outInt(a);
    outInt(b);
    return 0;
}
```

### Enums

Enums are a top level declarator in the same way as global variables and functions. They are declared as seen below.

```Rust
enum DaysOfWeek -> {
    MON, TUE, WED, THU, FRI, SAT, SUN
}
```

You can use them interchangeably with integers and chars, as they are just `i32`'s under the hood. Here is an example below

```Rust
fn main() -> int {
    let mut day: DaysOfWeek = DaysOfWeek.MON; 
    day += 1; // setting it to TUE
    if day == DaysOfWeek.TUE {
        outInt(2);
    }
}
```

## Operators

X supports all standard mathematical operators a language like C has. For a formal understanding of operator
precedence, check out the `grammar.md` file, but it can be summarised as below. All expressions are left-associative.

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

## Standard Library

Although quite primitive, we have some basic IO functions as a standard library.

`outInt` takes in an integer and prints it to stdout, followed by a newline.

`outChar` takes in a character and prints it to stdout, followed by a newline.

`outFloat` takes in a float and prints it to stdout, followed by a newline.

`outStr` takes in a string constant and prints it to stdout.
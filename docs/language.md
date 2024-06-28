# X Language Reference

## Hello World Program

```Rust
fn main() -> int {
    outStr("hello, world!\n");
    return 0;
}
```

To run this program, write it to a file called `hello.x` and compile it with the command line
arguments `hello.x -o hello`. The file `hello` will be generated as a binary executable.

## General Structure

An X program is composed of a series of global variable declarations and function declarations.
Variables are declared in a C-like manner, with the addition of a special optional `mut`. See the 
below mutability section for an explanation of this (it's the same semantics as Rust).

*Example Variable Declarations*

```Rust
mut int x = 1; // can be modified
bool isEven = x % 2 == 0; // cannot be modified, checked at compile time
str myName = "What is your name? ";
```

Below is an example of a function declaration. The pattern is quite clear, and follows standard C/Rust
like semantics. Note that function parameters are also delimited as being mutable or not.

*Example Function Declaration*

```Rust
fn foo(mut int x, bool y) -> void {
    x += 1;
    if x < 10 && y {
        outStr("Condition true");
    }
    return;
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

int x = 1;
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
mut int i = 0;
while i < 10 {
    outInt(i);
    i += 1;
}

// Permissible
mut int i = 0;
do {
    outInt(i);
    i += 1;
} while (i < 10);

// Permissible
mut int i;
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
structs. There are boolean expressions, denoted with the `bool` keyword, integers (32-bit signed) with the `int` keyword
and strings with the `str` keyword. All strings are constant at the moment, and can only be used in
simple I/O operations. Typecasting is not valid between any of these types currently.

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

`outInt` takes in an integer and prints it to stdout.

`inInt` takes in a string constant and an integer. It logs the string as a prompt, then reads an
integer from stdin and writes to that mutable variable.

`outStr` takes in a string constant and prints it to stdout.
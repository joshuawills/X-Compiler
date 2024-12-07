---
title: "Control Flow"
weight: 1
---

X has the standard control flow operators that other languages have, and behave the same semantically.
You have if statements, which can be succeeded by else-if and/or else statements. There is also
while statements, do-while statements, for-loops and, interestingly, loop statements. Conditions
do **not** need to be nested with brackets like they do in C/Java/etc., however the conditions themselves must be surrounded
by curly braces. This language also has the
`break` and `continue` keywords, with standard semantic usage. Note that because X has a native boolean
type, you can't do certain expressions that you can in C, such as

```C
// This is C code

let x: i64 = 1;
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
    io::print("A\n");    
} else if false {
    io::print("A\n");    
} else {
    io::print("A\n");    
}

// Permissible
let mut i: i64 = 0;
while i < 10 {
    io::println(i);
    i += 1;
}

// Permissible
let mut i: i64 = 0;
do {
    io::println(i);
    i += 1;
} while (i < 10);

// Permissible
let mut i: i64;
for i = 0; i < 10; i += 1 {
    io::println(i);
}

// Permissible, but not recommended from a style perspective
while (true) {
    io::println(2);
}

// Curly braces must encompass any control flow, even for single statement conditions
// Impermissible
if true
    io::print("A\n");
while false x = x + 1;
```

### Loop Statement

The vast majority of conditional logic can be handled with the `loop` keyword, and is the recommended 
approach where possible. Semantically, the loop keyword has many forms, which can be most easily expressed with examples.

```Rust
// Standard case
loop {
    io::print("This is an infinite loop");

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
    io::println($);
}

// Upper and lower limit
// This is semantically equivalent to "for $ = 1; $ < 10; $++"
loop 1 10 {
    io::println($);
}

// Custom variable
// This overrides the $ value to allow for more expressiveness
loop x {
    io::println(x);
}

// This case combines all these features into one example
loop x in 1 10 {
    io::println(x);
}
```

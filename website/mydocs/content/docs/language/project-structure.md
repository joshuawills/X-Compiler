---
title: "Project Structure"
weight: 1
---

A program is composed primarily of import/using statements, global variables, functions and type declarations. In the below
example, you can see a high level summary of how all these entities are structured.

All **import statements** and **using statements** must occur at the top of the file. All of the exported entities in the 
provided file path are loaded into the specified namespace, and can be later accessed with the 
program using the double colon notation.

User-specified types can currently come in two forms, simple **enum types** and **struct types**. Enum types 
are currently represented as simple integers under the hood. Structs can be nested, containing references to primitive.
and complex types alike. If you want to later mutate the elements of a struct in a program's runtime, the fields must
be specified as mutable.

**Global variables** are declared using the *let* keyword. The type does not need to be specified, as it can be inferred by the compiler,
but there are specific cases where it may be required, beyond for readability. In the below example, the *i32* type is specified as, by default,
the compiler assumes all integer expressions to be of type *i64*. The other alternative would be to type cast the expression.

**Functions** are specified with the *fn* keyword, followed by an identifier and a list of function parameters and a return type. 
Just like structs, mutable parameters must be explicitly declared, and this is checked at compile time. Functions can be overloaded
with different parameters as well.

The entry point for an application is the main function.

```Rust
// All imports must be placed at the top of the file before any other statement
// Paths should be relative to the position of the pertinent file 
// there will be further support for other paths later
import "../myLib/math.x" as Math;

// This shorthand can be specified for standard libraries
// The path to standard library can be specified with the 'X_LIB_PATH' env 
// variable, but defaults to "$HOME/.x-lib/" 
using std, io, math;

export enum Boolean -> {
    TRUE, FALSE
}

struct myData -> {
    myVec: Math::Vec2, // Externally declared entities are accepted via the user 
    // specified namespace and the '::' directive
    mut accessible: Boolean,
    myFavouriteNumber: i32
}

export fn logBoolean(b: Boolean) -> void {
    if b == Boolean.TRUE {
        io::println("true\n");
    } else {
        io::println("false\n");
    }
}

// Here the type is specified by you could alternatively do let myFavNumber = (i32) 21;
let myFavNumber: i32 = 21;

fn main() -> void {
    let d = myData {
        Math::Vec2 {2, 3},
        Boolean.TRUE,
        myFavNumber
    };

    logBoolean(d.accessible);
}

```
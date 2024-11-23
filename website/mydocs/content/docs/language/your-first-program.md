---
title: "Your First Program"
---

```Rust
// A hello world application :)
fn main() -> i64 {
    let val = "hello, world\n";
    outStr(val);
    return 0;
}
```

From this block of code, we can already deduce a lot about the general syntax of the language. X
is heavily inspired by the syntax of both Rust and C.

## Standard Library

Although quite primitive, we have some basic IO functions as a standard library.

`outI64` takes in an integer and prints it to stdout, followed by a newline.

`outChar` takes in a character and prints it to stdout, followed by a newline.

`outF32` takes in a float and prints it to stdout, followed by a newline.

`outF64` takes in a double and prints it to stdout, followed by a newline.

`outStr` takes in a string constant and prints it to stdout.

`malloc` takes in an integer and returns a pointer to newly allocated memory

`free` takes in a pointer and deallocates that memory

`size` can take in a type (primitive or constructed) or a variable and return its size
in bytes as an integer

`type` can take in an expr and return its type. Only works for primitive types (e.g. can't pass
in an array or a struct)
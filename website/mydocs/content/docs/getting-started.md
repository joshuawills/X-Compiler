---
title: "Getting Started"
weight: 1
---

*All development so far has been on Manjaro Linux. I cannot confirm yet whether or not this compiler
works correctly on other operating systems/environments*

## Pre-Requisites

- Java 21
- Gradle
- Git
- Clang 18.1.18 >

## Setup

Clone the `X-Compiler` repository onto your local machine

```bash
# HTTPS
git clone https://github.com/joshuawills/X-Compiler.git

# SSH
git clone git@github.com:joshuawills/X-Compiler.git
```

Build the project using `gradle build`

You should now have a jar build file located at `build/libs/X-Compiler-1.0-SNAPSHOT.jar`

You can invoke the program like so.

```bash
java -jar build/libs/X-Compiler-1.0-SNAPSHOT.jar
```

Alternately, which is the method I prefer, you can create an alias to this command and call it `xc`.
Place the alias in your shell startup file so that it always it set

```bash
alias XC="{path-to-project}/build/libs/X-Compiler-1.0-SNAPSHOT.jar" 
```

I recommend invoking the program with the `-h` flag to see all the options you can run the compiler
with. For a simple example, write the following content to a file called `hello.x`

```Rust
import std, io;

fn main() -> void {
    io::println("hello, world!");
}
```

Compile the program with the command `xc hello.x -o hello` and run the executable `hello` that is 
generated. Alternately, run the compiler with the additional `-r` flag to build and run the program
in succession.
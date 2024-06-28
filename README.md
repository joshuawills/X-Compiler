# The X-Compiler

This is a revamped version of the [old XY Compiler](https://github.com/joshuawills/XY-Compiler) that I decided to 
rewrite after learning more formal compiler development theory.

The language is a mix of C and Rust syntax, taking features I like from both.

At the moment the language is quite primitive. However, it has a strong foundation, and I have a clear vision
of where to take it. Around 70% of compilation time is currently delegated to Clang's backend work on my
LLVM output. A significant future improvement I am considering is a port over to generating native assembly code, x86_64 
to start with. However, I will continue generating LLVM IR for the time being given its simplicity.

## How it Works

The compiler goes through several successive stages, that all in some capacity depend on the preceding stage. There is a
formal grammar associated with the language, as well as some informal semantic rules (regarding mutability, etc). These
can be viewed in the [grammar.md](/docs/grammar.md) and [language.md](/docs/language.md) files respectively in the `/docs` folder. To view the `grammar.md`
output, which is formatted in Latex, you can install a Latex parser in your text editor. For VS Code, consider [this extension](https://marketplace.visualstudio.com/items?itemName=mathematic.vscode-latex).

## How to Use

I use Gradle to build my project, however you can just use the base Java compiler. The project is built with Java 21. It might work with older versions, I have yet to test that. For help with how to use the compiler, run the program with the "-h" or "--help" flags. For simple program examples, view the `/examples` folders in this repo.

To run the tests, chmod the `tests.sh` file and execute it. It, too, has a help flag, "-h", if you're interested in writing
tests for it.
# CHANGELOG

*31.05.2024*

- Set up basic CL args

*1.06.2024*

- Implement mutability checks in `Checker.java`
- Set up testing env - IN PROGRESS
- Begin code gen with LLVM IR

*15.06.2024*

- Minor error checks for unused variables/functions
- Minor error checks for unnecessary mutable declarations

*27.06.2024*

- developing loop stmts
- fixing variable shadowing
- Set up unique var support for loop stmts
  - Includes fixing grammar
- More advanced language features
  - %, +=, -=, /=, *=,
- Set up global vars properly
- In int

*28.06.2024*

- Do while construct

*29.06.2024*

- outStr() implementation and basic string declarations
- Allow for simple loop x {} case
 
*1.07.2024*

- comma separated variable declarations

*11.07.2024*

- some float stuff done

*26.07.2024*

- basic pointer support
- * types
- dereference and addr-of operands

*29.07.2024*

- basic array initialisation

*07.08.2024*

- remove math-decl stmts
- fix array access bug for local vars

*09.08.2024*

- char support mostly :)
- removing strings :(

*10.08.2024*

- switching to Rust style declarations 
- Enum support
- Basic Function overloading
- Fixed boolean short-circuiting
- More tests!

*13.08.24*

- complete test suite covering all errors
- can emit type declarations where possible
- prevent variables having the same name in IR
- optimisations by preventing duplicate string declarations
- porting logic from emitter to type checker
- enum Arrays

*14.08.2024*

- made a start on the great structs
 - can declare types with full validation of types/etc
 - next up is actually instantiating them :)
- set up the website

*15.08.2024*

- can instantiate structs!!
 - structs can contain structs and enums, etc
 - need to set up reading and writing to struct fields now :)
 - need a reassessment of my rejection of an assign-expr CFG element now

*06.11.2024*

- can instantiate nested structs
- can access structs

# TODO

- arrays of structs
- arrays in structs
- pointers in structs!

- float support
  - fix it so it works for them all! 
  - set up type checking without type conversion first

- in-compilation expression everywhere possible
- write some more test cases 
- basic string operations
  - static reassignment
    - mut str x = "a"; x = "b";
	- '+' operator
- basic control flow logic evaluation
  - if always false, don't generate logic
  - side effect detector
- errors messages regarding mutability/reassignment w/ pointers

- pointers and dynamic memory
- generics

- clean up all the todos
- set default values for uninitialised
- change strs to native char *
- underline relevant part in error messages

HERE

- pointer arithmetic
- assigning string literals
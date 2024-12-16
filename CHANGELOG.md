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

*08.11.2024*

- declare arrays of structs
- code improvements

*09.11.2024*

- more work on arrays...

*10.11.2024*

- more work on structs
  - arrays of structs
  - arrays in structs

*13.11.2024*

- begin working with modules and file imports
  - working for functions!
- size of operator
- return of outStr
- more work on arrays and struct types
- malloc and free can be called

*15.11.2024*

- bit of work cleaning up the codebase

*16.11.2024*

- floats properly work in the language now
- adding in new numerical types (partially done)

*17.11.2024*

- adding in new numerical types (f32/f64)
- new type() function

*20.11.2024*

- starting to write out standard libraries
- accessing lib c through '@' prefix of call expressions
- various bug fixes in expression evaluation

- passing struct types into functions

*23.11.2024*

- structs correctly pasted onto stack when passed through functions
- malloc and free moved to lib/std.x
- explicit type casting with 'as' keyword
- documentation updated
- pointers to struct accesses!

*26.11.2024*

- can use array indexes on pointers
- all arrays types bound to pointer variables
- basic tuple instantiation work

*27.11.2024*

- more work on tuples and structs returning from functions etc.

*05.12.2024*

- tuple destructuring support
- proper type analysis
  - remove need for 'as void*' everywhere

*06/7.12.2024*

- can access std lib without path resolution
- can do this pattern "import A, B;" for std library files

*08.12.2024*

- pointer arithmetic!!

*09.12.2024*

- min and max numeric values in std lib
- can't access '_' variables
- can have multiple variables with the same name in scope
- method declarations mapped to struct types

*12.12.2024*

- adding in argc/argv

*13.12.2024*

- supporting new vs code extension
- experimental support for method accesses 

*14.12.2024*

- able to map methods directlyt to raw expressions

*16.12.24*

- using statement working
  - TODO: update the vs code extension, the asset and the website
  - write some new examples

# TODO

- in-compilation expression everywhere possible

- basic string operations
	- '+' operator

- basic control flow logic evaluation
  - if always false, don't generate logic
  - side effect detector

- errors messages regarding mutability/reassignment w/ pointers

- generics

- clean up all the todos
- set default values for uninitialised
- underline relevant part in error messages

HERE

- do struct accesses, tuples in grammar
- general grammar overhaul
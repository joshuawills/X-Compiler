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

# TODO

- float support
  - set up syntax -- DONE
  - correctly lex them -- DONE
  - parsing should be straightforward -- DONE
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

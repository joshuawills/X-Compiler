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

# TODO

- outStr() implementation
- Comma separated variable declarations
- Allow for simple loop x {} case
- Float support
- In-compilation expression everywhere possible
- Basic control flow logic evaluation
  - if always false, don't generate logic
  - Side effect detector
- Write some more test cases 
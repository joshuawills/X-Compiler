// Can't access function that is not exported

import "standard.x" as std;

fn main() -> int {

    let x = std::add(1, 2);
    return 0;

}
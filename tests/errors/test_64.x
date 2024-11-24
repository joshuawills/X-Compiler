// Can't access function that is not exported

import "standard.x" as std;

fn main() -> void {

    let x = std::add(1, 2);

    let y = std::what.val;

    let z = std::MyBoolean.TRUE;

}
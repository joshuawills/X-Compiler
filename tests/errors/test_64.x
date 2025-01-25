// Can't access function that is not exported

import "standard.x" as std;
using "foo.x";

fn main() -> void {

    let x = std::add(1, 2);

    let y = std::what.val;

    let z = std::MyBoolean.TRUE;

    say_hello();

	let v = (21).subtract_one();

}

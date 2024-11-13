// No such variable in module

import "standard.x" as std;

fn main() -> int {
    let a = std::val;

    std::hello_world = "what!";

}
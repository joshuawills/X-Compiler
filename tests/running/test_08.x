// Allowing for multiple variables with the same name

import "../../lib/std.x" as std;

let i: i64 = 0;

fn main() -> void {
    std::println(i);
    let i: i64 = 21;
    std::println(i);
    {
        let i: i64 = 19;
        std::println(i);
    }

    {
        let i: i64 = 34;
        std::println(i);
        {
            let i: i64 = -100;
            std::println(i);
        }
    }

}
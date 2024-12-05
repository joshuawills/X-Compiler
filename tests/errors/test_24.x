// inappropriate use of the '$' operator

import "../../lib/std.x" as std;

fn main() -> void {

    let $: i64 = 21;


    loop i in 10 {
        std::println($);
    }

}

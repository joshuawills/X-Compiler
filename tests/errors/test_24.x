// inappropriate use of the '$' operator

import std;

fn main() -> void {

    let $: i64 = 21;


    loop i in 10 {
        std::println($);
    }

}

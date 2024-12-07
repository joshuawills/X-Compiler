// inappropriate use of the '$' operator

import std, io;

fn main() -> void {

    let $: i64 = 21;


    loop i in 10 {
        io::println($);
    }

}

// Allowing for multiple variables with the same name

import std, io;

let i: i64 = 0;

fn main() -> void {
    io::println(i);
    let i: i64 = 21;
    io::println(i);
    {
        let i: i64 = 19;
        io::println(i);
    }

    {
        let i: i64 = 34;
        io::println(i);
        {
            let i: i64 = -100;
            io::println(i);
        }
    }

}
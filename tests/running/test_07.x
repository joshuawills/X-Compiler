// Loop constructs in their different forms

import std, io;

fn main() -> void {

    let mut i: i64 = 0;
    while i < 10 {
        io::println(i);
        i += 1;
    }

    let mut j: i64 = -1;
    while j < 10 {
        if j == 5 {
            break;
        }
        j += 1;
        if j == 2 {
            continue;
        }
        io::println(j);
    }

    loop {
        if $ == 1 {
            continue;
        }
        io::println($);
        if $ == 3 {
            break;
        }
    }

    loop haha in 3 5 {
        io::println(haha);
    }

    io::println(-19);

    loop l_one in 5 {
        
        if l_one == 1 {
            break;
        }
        
        loop l_two in 3 {
            if l_two == 1 {
                continue;
            }
            io::println(l_one);
            io::println(l_two);
        }
    }

    let mut why_not: i64 = 0;

    do {
        io::println('x');
    } while why_not != 0;

    let mut donda: i64 = 0;
    for donda = 0; donda < 10; donda += 1 {
        io::println(donda);
    }

    let mut explanatory: i64 = 0;
    for ;; {
        io::println(explanatory);
        if explanatory == 3 {
            break;
        }
        explanatory += 1;
    }


}
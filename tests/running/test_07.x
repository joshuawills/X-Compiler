// Loop constructs in their different forms

fn main() -> int {

    let mut i: int = 0;
    while i < 10 {
        outInt(i);
        i += 1;
    }

    let mut j: int = -1;
    while j < 10 {
        if j == 5 {
            break;
        }
        j += 1;
        if j == 2 {
            continue;
        }
        outInt(j);
    }

    loop {
        if $ == 1 {
            continue;
        }
        outInt($);
        if $ == 3 {
            break;
        }
    }

    loop haha in 3 5 {
        outInt(haha);
    }

    outInt(-19);

    loop l_one in 5 {
        
        if l_one == 1 {
            break;
        }
        
        loop l_two in 3 {
            if l_two == 1 {
                continue;
            }
            outInt(l_one);
            outInt(l_two);
        }
    }

    let mut why_not: int = 0;

    do {
        outChar('x');
    } while why_not != 0;

    let mut donda: int = 0;
    for donda = 0; donda < 10; donda += 1 {
        outInt(donda);
    }

    let mut explanatory: int = 0;
    for ;; {
        outInt(explanatory);
        if explanatory == 3 {
            break;
        }
        explanatory += 1;
    }


}
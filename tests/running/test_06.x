// Logging global arrays, mutating them

let x: i64[] = [1, 19, 9];
let x1: bool[] = [true, false, true];
let mut x2: i8[] = ['a', 'b', 'c'];

fn main() -> i64 {

    loop i in 3 {
        outI64(x[i]);
    }

    loop y in 3 {
        if x1[y] {
            outI64(1);
        } else {
            outI64(0);
        }
    }

    loop z in 3 {
        outChar(x2[z]);
    }

    loop a in 3 {
        x2[a] = 'z';
    }

    loop b in 3 {
        outChar(x2[b]);
    }
}
// Emitting type annotations

enum Boolean -> { TRUE, FALSE }

let x = 21;
let y = true;
let z = 'a';
let a = Boolean.TRUE;

fn main() -> int {

    if y {
        outInt(x);
        outChar(z);
    }

    let arr = [1, 2, 3, 4, 5, 6];

    loop i in 6 {
        outInt(arr[i]);
    }

    // Casts all following types to the first one where possible
    let arr2 = [1, 'a', Boolean.TRUE];

    loop i in 3 {
        outInt(arr2[i]);
    }

}

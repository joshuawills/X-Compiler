// Global variables

let x: i64 = 21;
let mut x1: i64 = 19;

let y: char = 'a';
let mut y1: char = '&';

let z: bool = false;
let mut z1: bool = true;

let mut aA: i64[] = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];
let mut aB: bool[] = [true, false, true, false];
let mut aC: char[] = ['a', 'b', 'c', 'd'];

fn main() -> i64 {
    outInt(x); // 20
    outInt(x1); // 19
    x1 += 1;
    outInt(x1); // 20
    printX1(); // 20

    outChar(y); // 'a'
    outChar(y1); // '&'
    y1 += 1;
    outChar(y1); // '`';
    printY1(); // '`';

    outBool(z); // false
    outBool(z1); // true
    z1 = !z1;
    outBool(z1); // false;
    printZ1(); // false;

    loop i in 10 {
        outInt(aA[i]);
    }
    outInt(aA[0]);
    printaAZero();
    aA[0] = 19;
    printaAZero();

    loop j in 4 {
        outBool(aB[j]);
    }
    aB[0] = false;
    loop dummy in 4 {
        outBool(aB[dummy]);
    }

    loop whatj in 4 {
        outChar(aC[whatj]);
    }
    aC[0] = 'e';
    loop dummyd in 4 {
        outChar(aC[dummyd]);
    }
}

fn printaAZero() -> void {
    outInt(aA[0]);
}

fn printZ1() -> void {
    outBool(z1);
}

fn printX1() -> void {
    outInt(x1);
}

fn printY1() -> void {
    outChar(y1);
}


fn outBool(x: bool) -> void {
    if x {
        outInt(1);
    } else {
        outInt(0);
    }
}
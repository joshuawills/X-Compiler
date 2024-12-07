// Global variables

import std, io;

let x: i64 = 21;
let mut x1: i64 = 19;

let y: i8= 'a';
let mut y1: i8 = '&';

let z: bool = false;
let mut z1: bool = true;

let mut aA: i64[] = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];
let mut aB: bool[] = [true, false, true, false];
let mut aC: i8[] = ['a', 'b', 'c', 'd'];

fn main() -> void {
    io::println(x); // 20
    io::println(x1); // 19
    x1 += 1;
    io::println(x1); // 20
    printX1(); // 20

    io::println(y); // 'a'
    io::println(y1); // '&'
    y1 += 1;
    io::println(y1); // '`';
    printY1(); // '`';

    outBool(z); // false
    outBool(z1); // true
    z1 = !z1;
    outBool(z1); // false;
    printZ1(); // false;

    loop i in 10 {
        io::println(aA[i]);
    }
    io::println(aA[0]);
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
        io::println(aC[whatj]);
    }
    aC[0] = 'e';
    loop dummyd in 4 {
        io::println(aC[dummyd]);
    }
}

fn printaAZero() -> void {
    io::println(aA[0]);
}

fn printZ1() -> void {
    outBool(z1);
}

fn printX1() -> void {
    io::println(x1);
}

fn printY1() -> void {
    io::println(y1);
}


fn outBool(x: bool) -> void {
    if x {
        io::println(1);
    } else {
        io::println(0);
    }
}
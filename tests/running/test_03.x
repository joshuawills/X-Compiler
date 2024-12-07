// Boolean logic and conditional branching

import std, io;

fn doSomething() -> bool {
    io::println('z');
    return true;
}

fn main() -> void {

    if true && true {
        io::println(1);
    }

    if true && false {
        io::println(2);
    }

    if false && true {
        io::println(3);
    }

    if false && false {
        io::println(4);
    }

    if true && doSomething() {
        io::println(5);
    }

    if false && doSomething() {
        io::println(6);
    }

    if true || doSomething() {
        io::println(7);
    }

    if false || doSomething() {
        io::println(8);
    }

    if !false {
        io::println(9);
    }

    if !true {
        io::println(10);
    }

    if true || true {
        io::println(11);
    }

    if false || true {
        io::println(12);
    }

    if true || false {
        io::println(13);
    }

    if false || false {
        io::println(14);
    }
}
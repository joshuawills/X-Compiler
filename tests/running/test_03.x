// Boolean logic and conditional branching

import "../../lib/std.x" as std;

fn doSomething() -> bool {
    std::println('z');
    return true;
}

fn main() -> void {

    if true && true {
        std::println(1);
    }

    if true && false {
        std::println(2);
    }

    if false && true {
        std::println(3);
    }

    if false && false {
        std::println(4);
    }

    if true && doSomething() {
        std::println(5);
    }

    if false && doSomething() {
        std::println(6);
    }

    if true || doSomething() {
        std::println(7);
    }

    if false || doSomething() {
        std::println(8);
    }

    if !false {
        std::println(9);
    }

    if !true {
        std::println(10);
    }

    if true || true {
        std::println(11);
    }

    if false || true {
        std::println(12);
    }

    if true || false {
        std::println(13);
    }

    if false || false {
        std::println(14);
    }
}
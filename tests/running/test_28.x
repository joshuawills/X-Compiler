// Testing if-else constructs

import std, io;

fn main() -> void {

    if (true) {
        io::println(-2);
    } else {
        io::println(-1);
    }

    if (false) {
        io::println(1);
    } else {
        io::println(2);
    }

    if (false) {
        io::println(5);
    } else if (false) {
        io::println(6);
    } else {
        io::println(7);
    }

    if (true) {
        io::println(8);
    } else if (false) {
        io::println(9);
    } else {
        io::println(10);
    }

    if (true) {
        io::println(11);
    } else if (true) {
        io::println(12);
    } else {
        io::println(13);
    }

}
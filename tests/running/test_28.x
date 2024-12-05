// Testing if-else constructs

import "../../lib/std.x" as std;

fn main() -> void {

    if (true) {
        std::println(-2);
    } else {
        std::println(-1);
    }

    if (false) {
        std::println(1);
    } else {
        std::println(2);
    }

    if (false) {
        std::println(5);
    } else if (false) {
        std::println(6);
    } else {
        std::println(7);
    }

    if (true) {
        std::println(8);
    } else if (false) {
        std::println(9);
    } else {
        std::println(10);
    }

    if (true) {
        std::println(11);
    } else if (true) {
        std::println(12);
    } else {
        std::println(13);
    }

}
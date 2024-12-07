// Enums in other files accessed!

import std, io;

import "math-what.x" as Math;

fn main() -> void {

        let mut val = Math::Boolean.TRUE;

        if val == Math::Boolean.TRUE {
                io::println(1);
        }

        val = Math::Boolean.FALSE;

        if val == Math::Boolean.FALSE {
                io::println(1);
        }
}

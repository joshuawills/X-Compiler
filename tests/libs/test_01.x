// Testing sin and cos in the math lib

import std, io, math;

fn main() -> void {

    let pi = Math::PI;

    io::print("===\n");
    io::print(Math::sin(0.0));
    io::print(Math::sin(pi / 2.0));
    io::print(Math::sin(pi));
    io::print(Math::sin(pi + pi / 2.0));
    io::print(Math::sin(2.0 * pi));

    let mut degreesOption = Math::TrigOptions.DEGREES;
    io::print("===\n");

    io::print(Math::sin(0.0, degreesOption));
    io::print(Math::sin(90.0, degreesOption));
    io::print(Math::sin(180.0, degreesOption));
    io::print(Math::sin(270.0, degreesOption));
    io::print(Math::sin(360.0, degreesOption));
    io::print("===\n");

    degreesOption = Math::TrigOptions.RADIANS;

    io::print(Math::sin(0.0, degreesOption));
    io::print(Math::sin(pi / 2.0, degreesOption));
    io::print(Math::sin(pi, degreesOption));
    io::print(Math::sin(pi + pi / 2.0, degreesOption));
    io::print(Math::sin(2.0 * pi, degreesOption));
    io::print("===\n");

    io::print("===\n");
    io::print(Math::cos(0.0));
    io::print(Math::cos(pi / 2.0));
    io::print(Math::cos(pi));
    io::print(Math::cos(pi + pi / 2.0));
    io::print(Math::cos(2.0 * pi));

    degreesOption = Math::TrigOptions.DEGREES;
    io::print("===\n");

    io::print(Math::cos(0.0, degreesOption));
    io::print(Math::cos(90.0, degreesOption));
    io::print(Math::cos(180.0, degreesOption));
    io::print(Math::cos(270.0, degreesOption));
    io::print(Math::cos(360.0, degreesOption));
    io::print("===\n");

    degreesOption = Math::TrigOptions.RADIANS;

    io::print(Math::cos(0.0, degreesOption));
    io::print(Math::cos(pi / 2.0, degreesOption));
    io::print(Math::cos(pi, degreesOption));
    io::print(Math::cos(pi + pi / 2.0, degreesOption));
    io::print(Math::cos(2.0 * pi, degreesOption));
    io::print("===\n");

}

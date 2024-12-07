// Testing sin and cos in the math lib

import math;
import std;

fn main() -> void {

    let pi = Math::PI;

    std::print("===\n");
    std::print(Math::sin(0.0));
    std::print(Math::sin(pi / 2.0));
    std::print(Math::sin(pi));
    std::print(Math::sin(pi + pi / 2.0));
    std::print(Math::sin(2.0 * pi));

    let mut degreesOption = Math::TrigOptions.DEGREES;
    std::print("===\n");

    std::print(Math::sin(0.0, degreesOption));
    std::print(Math::sin(90.0, degreesOption));
    std::print(Math::sin(180.0, degreesOption));
    std::print(Math::sin(270.0, degreesOption));
    std::print(Math::sin(360.0, degreesOption));
    std::print("===\n");

    degreesOption = Math::TrigOptions.RADIANS;

    std::print(Math::sin(0.0, degreesOption));
    std::print(Math::sin(pi / 2.0, degreesOption));
    std::print(Math::sin(pi, degreesOption));
    std::print(Math::sin(pi + pi / 2.0, degreesOption));
    std::print(Math::sin(2.0 * pi, degreesOption));
    std::print("===\n");

    std::print("===\n");
    std::print(Math::cos(0.0));
    std::print(Math::cos(pi / 2.0));
    std::print(Math::cos(pi));
    std::print(Math::cos(pi + pi / 2.0));
    std::print(Math::cos(2.0 * pi));

    degreesOption = Math::TrigOptions.DEGREES;
    std::print("===\n");

    std::print(Math::cos(0.0, degreesOption));
    std::print(Math::cos(90.0, degreesOption));
    std::print(Math::cos(180.0, degreesOption));
    std::print(Math::cos(270.0, degreesOption));
    std::print(Math::cos(360.0, degreesOption));
    std::print("===\n");

    degreesOption = Math::TrigOptions.RADIANS;

    std::print(Math::cos(0.0, degreesOption));
    std::print(Math::cos(pi / 2.0, degreesOption));
    std::print(Math::cos(pi, degreesOption));
    std::print(Math::cos(pi + pi / 2.0, degreesOption));
    std::print(Math::cos(2.0 * pi, degreesOption));
    std::print("===\n");

}

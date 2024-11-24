// Testing sin and cos in the math lib

import "../../lib/math.x" as Math;

fn main() -> void {

    let pi = Math::PI;

    outStr("===\n");
    outF64(Math::sin(0.0));
    outF64(Math::sin(pi / 2.0));
    outF64(Math::sin(pi));
    outF64(Math::sin(pi + pi / 2.0));
    outF64(Math::sin(2.0 * pi));

    let mut degreesOption = Math::TrigOptions.DEGREES;
    outStr("===\n");

    outF64(Math::sin(0.0, degreesOption));
    outF64(Math::sin(90.0, degreesOption));
    outF64(Math::sin(180.0, degreesOption));
    outF64(Math::sin(270.0, degreesOption));
    outF64(Math::sin(360.0, degreesOption));
    outStr("===\n");

    degreesOption = Math::TrigOptions.RADIANS;

    outF64(Math::sin(0.0, degreesOption));
    outF64(Math::sin(pi / 2.0, degreesOption));
    outF64(Math::sin(pi, degreesOption));
    outF64(Math::sin(pi + pi / 2.0, degreesOption));
    outF64(Math::sin(2.0 * pi, degreesOption));
    outStr("===\n");

    outStr("===\n");
    outF64(Math::cos(0.0));
    outF64(Math::cos(pi / 2.0));
    outF64(Math::cos(pi));
    outF64(Math::cos(pi + pi / 2.0));
    outF64(Math::cos(2.0 * pi));

    degreesOption = Math::TrigOptions.DEGREES;
    outStr("===\n");

    outF64(Math::cos(0.0, degreesOption));
    outF64(Math::cos(90.0, degreesOption));
    outF64(Math::cos(180.0, degreesOption));
    outF64(Math::cos(270.0, degreesOption));
    outF64(Math::cos(360.0, degreesOption));
    outStr("===\n");

    degreesOption = Math::TrigOptions.RADIANS;

    outF64(Math::cos(0.0, degreesOption));
    outF64(Math::cos(pi / 2.0, degreesOption));
    outF64(Math::cos(pi, degreesOption));
    outF64(Math::cos(pi + pi / 2.0, degreesOption));
    outF64(Math::cos(2.0 * pi, degreesOption));
    outStr("===\n");

}

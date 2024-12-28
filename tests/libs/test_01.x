// Testing sin and cos in the math lib

using std, io, math;

fn main() -> void {

    let pi = PI;

    println("===");
    println(sin(0.0));
    println(sin(pi / 2.0));
    println(sin(pi));
    println(sin(pi + pi / 2.0));
    println(sin(2.0 * pi));

    let mut degreesOption = TrigOptions.DEGREES;
    println("===");

    println(sin(0.0, degreesOption));
    println(sin(90.0, degreesOption));
    println(sin(180.0, degreesOption));
    println(sin(270.0, degreesOption));
    println(sin(360.0, degreesOption));
    println("===");

    degreesOption = TrigOptions.RADIANS;

    println(sin(0.0, degreesOption));
    println(sin(pi / 2.0, degreesOption));
    println(sin(pi, degreesOption));
    println(sin(pi + pi / 2.0, degreesOption));
    println(sin(2.0 * pi, degreesOption));
    println("===");

    println("===");
    println(cos(0.0));
    println(cos(pi / 2.0));
    println(cos(pi));
    println(cos(pi + pi / 2.0));
    println(cos(2.0 * pi));

    degreesOption = TrigOptions.DEGREES;
    println("===");

    println(cos(0.0, degreesOption));
    println(cos(90.0, degreesOption));
    println(cos(180.0, degreesOption));
    println(cos(270.0, degreesOption));
    println(cos(360.0, degreesOption));
    println("===");

    degreesOption = TrigOptions.RADIANS;

    println(cos(0.0, degreesOption));
    println(cos(pi / 2.0, degreesOption));
    println(cos(pi, degreesOption));
    println(cos(pi + pi / 2.0, degreesOption));
    println(cos(2.0 * pi, degreesOption));
    println("===");

}

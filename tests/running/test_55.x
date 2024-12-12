// Testing command line arguments

import io;

fn main(argc: i32, mut argv: i8**) -> void {

    io::print("Number of args provided: ");
    io::println(argc - 1);

    *argv += 1;
    while *argv != null {
        io::println(*argv);
        *argv += 1;
    }

}

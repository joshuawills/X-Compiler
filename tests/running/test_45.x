// Basic tuple operations

import std, io;

fn takeInTuple(mut tuple: (i64, bool, i8*)) -> void {
    io::print(tuple.2);
    tuple.0 += 2;
    io::println(tuple.0);
}

fn main() -> void {

    let mut a = (1, false, "hello world\n");

    if !a.1 {
        io::print(a.2);
    }

    a.0 += 2;
    
    if a.0 == 3 {
        io::print("Success");
    }

    takeInTuple(a);
    io::println(a.0);

}
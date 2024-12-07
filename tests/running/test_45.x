// Basic tuple operations

import std;

fn takeInTuple(mut tuple: (i64, bool, i8*)) -> void {
    std::print(tuple.2);
    tuple.0 += 2;
    std::println(tuple.0);
}

fn main() -> void {

    let mut a = (1, false, "hello world\n");

    if !a.1 {
        std::print(a.2);
    }

    a.0 += 2;
    
    if a.0 == 3 {
        std::print("Success");
    }

    takeInTuple(a);
    std::println(a.0);

}
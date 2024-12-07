// Structs passed to functions as values

import std, io;

struct Test -> {
	mut a: i64,
	b: i64
}

struct DeeperStruct -> {
    accessible: bool,
    mut test: Test
}

fn printData(mut data: Test) -> void {
	data.a += 1;
	io::print("should be 2: ");
	io::println(data.a);
}

fn printDeeperData(mut data: DeeperStruct) -> void {
    if data.accessible {
        io::print("should be 2: ");
        data.test.a += 1;
        io::println(data.test.a);
    } else {
        io::print("inaccessible");
    }
}


fn main() -> void {

	let data = Test { 1, 2 };
	io::print("should be 1: ");
	io::println(data.a);

	printData(data);
	io::print("should be 1: ");
	io::println(data.a);

    let data2 = DeeperStruct { true, Test { 1, 2 } };
    printDeeperData(data2);

    let data3 = DeeperStruct { false, Test { 1, 2 } };
    printDeeperData(data3);

}

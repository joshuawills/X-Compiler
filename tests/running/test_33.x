// Structs passed to functions as values

import "../../lib/std.x" as std;

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
	std::print("should be 2: ");
	std::println(data.a);
}

fn printDeeperData(mut data: DeeperStruct) -> void {
    if data.accessible {
        std::print("should be 2: ");
        data.test.a += 1;
        std::println(data.test.a);
    } else {
        std::print("inaccessible");
    }
}


fn main() -> void {

	let data = Test { 1, 2 };
	std::print("should be 1: ");
	std::println(data.a);

	printData(data);
	std::print("should be 1: ");
	std::println(data.a);

    let data2 = DeeperStruct { true, Test { 1, 2 } };
    printDeeperData(data2);

    let data3 = DeeperStruct { false, Test { 1, 2 } };
    printDeeperData(data3);

}

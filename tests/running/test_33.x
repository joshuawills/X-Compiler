// Structs passed to functions as values

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
	outStr("should be 2: ");
	outI64(data.a);
}

fn printDeeperData(mut data: DeeperStruct) -> void {
    if data.accessible {
        outStr("should be 2: ");
        data.test.a += 1;
        outI64(data.test.a);
    } else {
        outStr("inaccessible");
    }
}


fn main() -> void {

	let data = Test { 1, 2 };
	outStr("should be 1: ");
	outI64(data.a);

	printData(data);
	outStr("should be 1: ");
	outI64(data.a);

    let data2 = DeeperStruct { true, Test { 1, 2 } };
    printDeeperData(data2);

    let data3 = DeeperStruct { false, Test { 1, 2 } };
    printDeeperData(data3);

}

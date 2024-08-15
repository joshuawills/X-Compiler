// Instantiating different basic structs

enum DaysOfWeek -> {
    MON, TUE, WED, THU, FRI, SAT, SUN
}

struct BasicStruct -> {
    x: int,
    y: int
}

struct StructWithEnumAndOtherStruct -> {
    day: DaysOfWeek,
    data: BasicStruct
}

struct StructWithPointers -> {
    data: int*
}

fn main() -> int {

    let data = BasicStruct { 2, 3 };

    let data2 = StructWithEnumAndOtherStruct {
        DaysOfWeek.MON,
        data
    };

    let mut x = 2;
    let data3 = StructWithPointers { &x };

}
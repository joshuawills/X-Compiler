// Instantiating different basic structs

enum DaysOfWeek -> {
    MON, TUE, WED, THU, FRI, SAT, SUN
}

struct BasicStruct -> {
    x: i64,
    y: i64
}

struct StructWithEnumAndOtherStruct -> {
    day: DaysOfWeek,
    data: BasicStruct
}

struct StructWithPointers -> {
    data: i64*
}

fn main() -> i64 {

    let data = BasicStruct { 2, 3 };

    let data2 = StructWithEnumAndOtherStruct {
        DaysOfWeek.MON,
        data
    };

    let mut x = 2;
    let data3 = StructWithPointers { &x };

}
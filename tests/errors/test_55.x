// Nested structs impermissible in enums

enum Boolean -> { TRUE, FALSE }

fn main() -> i64 {

    let what = Boolean.TRUE.a;

}
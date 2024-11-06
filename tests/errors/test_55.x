// Nested structs impermissible in enums

enum Boolean -> { TRUE, FALSE }

fn main() -> int {

    let what = Boolean.TRUE.a;

}
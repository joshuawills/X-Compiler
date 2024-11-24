// Nested structs impermissible in enums

enum Boolean -> { TRUE, FALSE }

fn main() -> void {

    let what = Boolean.TRUE.a;

}
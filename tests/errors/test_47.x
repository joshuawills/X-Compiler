// Multiple type definitions with same name

enum A -> { B }
enum A -> { C }

struct B -> { x: i64 }
struct B -> { y: i64 }

struct C -> { x: i64 }
enum C -> { WHAT }

enum D -> { WHAT }
struct D -> { x: i64 }

fn main() -> void {

}

// Multiple type definitions with same name

enum A -> { B }
enum A -> { C }

struct B -> { x: int }
struct B -> { y: int }

struct C -> { x: int }
enum C -> { WHAT }

enum D -> { WHAT }
struct D -> { x: int }

fn main() -> int {

}

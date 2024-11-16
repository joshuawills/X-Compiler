
export let hello_world: char* = "hello, world!\n";

export let number = 21;

enum MyBoolean -> { TRUE, FALSE }

struct A -> { val: i64 }

let what = A { 2 };

fn add(x: i64, y: i64) -> i64 {
    return x + y;
}
// Variable declared mutable but never reassigned

let mut x: i8[] = ['a', 'b', 'c'];

fn main() -> void {

    let mut y: bool = x[0] == 'a';

    if !y {
    }

}
// Variable declared mutable but never reassigned

let mut x: char[] = ['a', 'b', 'c'];

fn main() -> i64 {

    let mut y: bool = false;

    outChar(x[0]);
    if !y {
        outInt(2);
    }

}
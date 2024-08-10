// operator precedence and maths

fn main() -> int {

    let x: int = 19;
    let y: int = 21;

    outInt(2 + x - 19);
    outInt(19 * y % x);
    outInt(x + y * 2);
    outInt((x + y) * 2);
    outInt(x - -1);
    outInt(-y * -19);
    outInt(x + y - 21 * 41 / 3);
}
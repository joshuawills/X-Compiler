// operator precedence and maths

fn main() -> i64 {

    let x: i64 = 19;
    let y: i64 = 21;

    outInt(2 + x - 19);
    outInt(19 * y % x);
    outInt(x + y * 2);
    outInt((x + y) * 2);
    outInt(x - -1);
    outInt(-y * -19);
    outInt(x + y - 21 * 41 / 3);
}
// operator precedence and maths

fn main() -> i64 {

    let x: i64 = 19;
    let y: i64 = 21;

    outI64(2 + x - 19);
    outI64(19 * y % x);
    outI64(x + y * 2);
    outI64((x + y) * 2);
    outI64(x - -1);
    outI64(-y * -19);
    outI64(x + y - 21 * 41 / 3);
}
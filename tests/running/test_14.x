// Euclid

fn main() -> int {

    let x: int = 6;
    let y: int = 2;

    if x <= 0 || y <= 0 {
        return 1;
    }

    outInt(euclid(x, y));
    return 0;
}

fn euclid(x: int, y: int) -> int {
    if x == 0 {
        return y;
    }
    return euclid(y % x, x);
}
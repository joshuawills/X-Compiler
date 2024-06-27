fn main() -> int {

    mut int x;
    mut int y;

    inInt("Enter a positive number here: ", x);
    inInt("Enter a positive number here: ", y);

    if x <= 0 || y <= 0 {
        return 1;
    }

    outInt(euclid(x, y));
    return 0;
}

fn euclid(int x, int y) -> int {
    if x == 0 {
        return y;
    }
    return euclid(y % x, x);
}
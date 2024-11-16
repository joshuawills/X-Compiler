// Testing if-else constructs

fn main() -> i64 {

    if (true) {
        outInt(-2);
    } else {
        outInt(-1);
    }

    if (false) {
        outInt(1);
    } else {
        outInt(2);
    }

    if (false) {
        outInt(5);
    } else if (false) {
        outInt(6);
    } else {
        outInt(7);
    }

    if (true) {
        outInt(8);
    } else if (false) {
        outInt(9);
    } else {
        outInt(10);
    }

    if (true) {
        outInt(11);
    } else if (true) {
        outInt(12);
    } else {
        outInt(13);
    }

}
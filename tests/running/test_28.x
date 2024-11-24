// Testing if-else constructs

fn main() -> void {

    if (true) {
        outI64(-2);
    } else {
        outI64(-1);
    }

    if (false) {
        outI64(1);
    } else {
        outI64(2);
    }

    if (false) {
        outI64(5);
    } else if (false) {
        outI64(6);
    } else {
        outI64(7);
    }

    if (true) {
        outI64(8);
    } else if (false) {
        outI64(9);
    } else {
        outI64(10);
    }

    if (true) {
        outI64(11);
    } else if (true) {
        outI64(12);
    } else {
        outI64(13);
    }

}
// Boolean logic and conditional branching

fn doSomething() -> bool {
    outChar('z');
    return true;
}

fn main() -> void {

    if true && true {
        outI64(1);
    }

    if true && false {
        outI64(2);
    }

    if false && true {
        outI64(3);
    }

    if false && false {
        outI64(4);
    }

    if true && doSomething() {
        outI64(5);
    }

    if false && doSomething() {
        outI64(6);
    }

    if true || doSomething() {
        outI64(7);
    }

    if false || doSomething() {
        outI64(8);
    }

    if !false {
        outI64(9);
    }

    if !true {
        outI64(10);
    }

    if true || true {
        outI64(11);
    }

    if false || true {
        outI64(12);
    }

    if true || false {
        outI64(13);
    }

    if false || false {
        outI64(14);
    }
}
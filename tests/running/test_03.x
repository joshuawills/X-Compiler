// Boolean logic and conditional branching

fn doSomething() -> bool {
    outChar('z');
    return true;
}

fn main() -> int {

    if true && true {
        outInt(1);
    }

    if true && false {
        outInt(2);
    }

    if false && true {
        outInt(3);
    }

    if false && false {
        outInt(4);
    }

    if true && doSomething() {
        outInt(5);
    }

    if false && doSomething() {
        outInt(6);
    }

    if true || doSomething() {
        outInt(7);
    }

    if false || doSomething() {
        outInt(8);
    }

    if !false {
        outInt(9);
    }

    if !true {
        outInt(10);
    }

    if true || true {
        outInt(11);
    }

    if false || true {
        outInt(12);
    }

    if true || false {
        outInt(13);
    }

    if false || false {
        outInt(14);
    }
}
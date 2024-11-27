// Basic tuple operations

fn takeInTuple(mut tuple: (i64, bool, i8*)) -> void {
    outStr(tuple.2);
    tuple.0 += 2;
    outI64(tuple.0);
}

fn main() -> void {

    let mut a = (1, false, "hello world\n");

    if !a.1 {
        outStr(a.2);
    }

    a.0 += 2;
    
    if a.0 == 3 {
        outStr("Success");
    }

    takeInTuple(a);
    outI64(a.0);

}
// Redeclaring char*

fn main() -> i64 {
    let mut val = "hello, world\n";

    outStr(val);
    
    val = "goodbye, world!\n";

    outStr(val);

    return 0;
}

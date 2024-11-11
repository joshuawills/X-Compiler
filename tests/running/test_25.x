// Redeclaring char*

fn main() -> int {
    let mut val = "hello, world\n";

    outStr(val);
    
    val = "goodbye, world!\n";

    outStr(val);

    return 0;
}

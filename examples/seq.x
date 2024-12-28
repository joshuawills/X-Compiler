using conv, io, str, std;

fn main(argc: i32, mut argv: i8**) -> void {

    let mut lower_limit: i64 = 1;
    let mut upper_limit: i64 = 100;
    let mut increment: i64 = 1;

    if argc == 2 {
        let mut val, _ = (argv[1]).to_i64();
        upper_limit = val;

    } else if argc == 3 {
        let mut val, _ = (argv[1]).to_i64();
        lower_limit = val;
        let mut val, _ = (argv[2]).to_i64();
        upper_limit = val;

    } else if argc == 4 {
        let mut val, _ = (argv[1]).to_i64();
        lower_limit = val;

        let mut val, _ = (argv[2]).to_i64();
        increment = val;

        let mut val, _ = (argv[3]).to_i64();
        upper_limit = val;
    } else {
        println("Usage: seq [first [increment]] last");
        exit(1);
    }
    
    
    if increment < 0 && lower_limit < upper_limit {
        exit(0);
    }

    if increment > 0 && lower_limit > upper_limit {
        exit(0);
    }

    let mut i: i64;
    for (i = lower_limit; i <= upper_limit; i += increment) {
        println(i);
    }

}
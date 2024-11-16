// Allowing for multiple variables with the same name

let i: i64 = 0;

fn main() -> i64 {
    outI64(i);
    let i: i64 = 21;
    outI64(i);
    {
        let i: i64 = 19;
        outI64(i);
    }

    {
        let i: i64 = 34;
        outI64(i);
        {
            let i: i64 = -100;
            outI64(i);
        }
    }

}
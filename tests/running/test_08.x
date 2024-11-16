// Allowing for multiple variables with the same name

let i: i64 = 0;

fn main() -> i64 {
    outInt(i);
    let i: i64 = 21;
    outInt(i);
    {
        let i: i64 = 19;
        outInt(i);
    }

    {
        let i: i64 = 34;
        outInt(i);
        {
            let i: i64 = -100;
            outInt(i);
        }
    }

}
// Allowing for multiple variables with the same name

let i: int = 0;

fn main() -> int {
    outInt(i);
    let i: int = 21;
    outInt(i);
    {
        let i: int = 19;
        outInt(i);
    }

    {
        let i: int = 34;
        outInt(i);
        {
            let i: int = -100;
            outInt(i);
        }
    }

}
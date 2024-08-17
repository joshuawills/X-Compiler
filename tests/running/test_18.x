// Multiple assignments

let mut array = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];

fn main() -> int {

    array[0] = array[1] = 3;

    loop i in 10 {
        outInt(array[i]);
    }

    array[0] = array[1] = array[2] = array[3] = array[4]
        = array[5] = array[6] = array[7] = array[8] = array[9] = 0;

    loop i in 10 {
        outInt(array[i]);
    }

    let mut y = 21;
    let mut z = 19;

    y = z -= 2;

    outInt(y);
    outInt(z);
}
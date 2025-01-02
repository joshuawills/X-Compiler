// Program to estimate PI using the monte carlo approximation

using std, rand, libc, math, io;

let INTERVAL: i64 = 10000;

fn main() -> void {

    let vp: void* = null;
    seed_random(@time(vp) as i32);

    let mut circle_points: u64 = 0;
    let mut square_points: u64 = 0;

    loop i in (INTERVAL * INTERVAL) {
        let top = get_random_int() % (INTERVAL + 1) as f64;
        let rand_x: f64 = top / INTERVAL;
        let top = get_random_int() % (INTERVAL + 1) as f64;
        let rand_y: f64 = top / INTERVAL;
        
        let origin_dist = rand_x.square() + rand_y.square();
 
        if origin_dist <= 1 {
            circle_points += 1;
        }
        square_points += 1;

    }

    let pi: f64 = (4 * circle_points) / square_points;
    print("Estimation of pi is ");
    println(pi);

}
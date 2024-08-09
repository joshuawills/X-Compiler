fn main() -> int {
    mut int x[] = [2, 3, 1, 4, 5, 9, 8, 7, 6, 0];
    print_arr(x, 10);
    bubble_sort(x, 10);
    print_arr(x, 10);
}

fn print_arr(int x[], int len) -> void {
    loop i in len { outInt(x[i]); }
}

fn bubble_sort(mut int x[], int len) -> void {
    mut bool swapped = false;
    loop i in (len - 1) {
        swapped = false;
        loop j in (len - i - 1) {
            if x[j] > x[j + 1] {
                int temp = x[j];
                x[j] = x[j + 1];
                x[j + 1] = temp;
                swapped = true;
            }
        }
        if !swapped {
            break;
        }
    }
}
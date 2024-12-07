---
title: 'Bubble Sort'
weight: 1
---

### With typed variables

```Rust
fn main() -> void {
    let mut x: i64[] = [2, 3, 1, 4, 5, 9, 8, 7, 6, 0];
    print_arr(x, 10);
    bubble_sort(x, 10);
    print_arr(x, 10);
}

fn print_arr(x: i64[], len: i64) -> void {
     loop i in len { io::println(x[i]); }
}

fn bubble_sort(mut x: i64[], len: i64) -> void {
    let mut swapped: bool = false;
    loop i in (len - 1) {
        swapped = false;
        loop j in (len - i - 1) {
            if x[j] > x[j + 1] {
                let temp: i64 = x[j];
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
```

### Without typed variables

```Rust
fn main() -> void {
    let mut x = [2, 3, 1, 4, 5, 9, 8, 7, 6, 0];
    print_arr(x, 10);
    bubble_sort(x, 10);
    print_arr(x, 10);
}

fn print_arr(x: i64[], len: i64) -> void {
     loop i in len { io::println(x[i]); }
}

fn bubble_sort(mut x: i64[], len: i64) -> void {
    let mut swapped = false;
    loop i in len - 1 {
        swapped = false;
        loop j in len - i - 1 {
            if x[j] > x[j + 1] {
                let temp = x[j];
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
```
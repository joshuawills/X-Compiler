// Testing inappropriate use of pointer access

enum What -> { A, B, C }

struct Node -> {
    value: i64
}

fn main() -> i64{

    let what = What->A;

    let node = Node { 21 };
    let what2 = node->value;

}
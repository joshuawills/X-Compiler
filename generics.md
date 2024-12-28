# Generics

## Sources

- https://en.wikipedia.org/wiki/Monomorphization

## Structs

struct LinkedList<T> -> {
    size: u64,
    head: Node<T>*
}

struct Node<T> -> {
    val: T,
    next: Node<T>*
}

---

struct Pair<T, V> -> {
    a: T,
    b: V
}

## Functions

fn size<T>(v: LinkedList<T>*) -> u64 {
    return v->size;
}

fn it<T>(v: T) -> T {
    return v;
}

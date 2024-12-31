# Generics

## Sources

- https://en.wikipedia.org/wiki/Monomorphization

## Structs

```Rust
struct LinkedList<T> -> {
    size: u64,
    head: Node<T>*
}

struct Node<T> -> {
    val: T,
    next: Node<T>*
}
```

---

```Rust
struct Pair<T, V> -> {
    a: T,
    b: V
}
```

## Functions

```Rust
fn size<T>(v: LinkedList<T>*) -> u64 {
    return v->size;
}

fn id<T>(v: T) -> T {
    return v;
}
```

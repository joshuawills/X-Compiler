---
title: 'Linked List'
weight: 1
---

```Rust
import std, io;

struct Node -> {
    mut val: i64,
    mut next: Node*
}

struct LinkedList -> {
    mut head: Node*,
    mut len: i64
}

fn makeLinkedList() -> LinkedList* {
    let mut val: LinkedList* = std::malloc(size(LinkedList));
    val->len = 0;
    val->head = null;
    return val;
}

fn newNode(val: i64) -> Node* {
    let mut newNode: Node* = std::malloc(size(Node));
    newNode->val = val;
    newNode->next = null;
    return newNode;
}

fn addNumToLinkedList(list: LinkedList*, val: i64) -> void {
    let mut node = newNode(val);

    if list->head == null {

        list->head = node;	
        list->len = 1;

    } else {

        let mut curr: Node* = list->head;
        while curr->next != null {
            curr = curr->next;
        }
        curr->next = node;
        list->len += 1;

    }

}

fn printLinkedList(list: LinkedList*) -> void {
    let mut curr: Node* = list->head;
    while curr != null {
        io::println(curr->val);
        curr = curr->next;
    }
    io::print("Size is: ");
    io::println(list->len);
}

fn freeLinkedList(list: LinkedList*) -> void {
    let mut curr: Node* = list->head;
    while curr != null {
        let mut next: Node* = curr->next;
        std::free(curr);
        curr = next;
    }
    std::free(list);
}


fn main() -> void {

    let list: LinkedList* = makeLinkedList();
    loop 1000 {
        addNumToLinkedList(list, $);
    }
    printLinkedList(list);

    freeLinkedList(list);

}
```

Below version has minimal type annotations and uses methods

```Rust
import std, io;

struct Node -> {
    mut val: i64,
    mut next: Node*
}

struct LinkedList -> {
    mut head: Node*,
    mut len: i64
}

fn (list: LinkedList*) free() -> void {
    let mut curr = list->head;
    while curr != null {
        let mut next = curr->next;
        std::free(curr);
        curr = next;
    }
    std::free(list);
}

fn LinkedList() -> LinkedList* {
    let mut val: LinkedList* = std::malloc(size(LinkedList));
    val->len = 0;
    val->head = null;
    return val;
}

fn Node(val: i64) -> Node* {
    let mut newNode: Node* = std::malloc(size(Node));
    newNode->val = val;
    newNode->next = null;
    return newNode;
}

fn (list: LinkedList*) concat(val: i64) -> void {
    let mut node = Node(val);
    if list->head == null {
        list->head = node;	
        list->len = 1;
    } else {
        let mut curr = list->head;
        while curr->next != null {
            curr = curr->next;
        }
        curr->next = node;
        list->len += 1;
    }
}

fn (list: LinkedList*) print() -> void {
    let mut curr = list->head;
    while curr != null {
        io::println(curr->val);
        curr = curr->next;
    }
    io::print("Size is: ");
    io::println(list->len);
}

fn (list: LinkedList*) contains(val: i64) -> bool {
    let mut curr = list->head;
    while curr != null {
        if curr->val == val {
            return true;
        }
        curr = curr->next;
    }
    return false;
}

fn main() -> void {

    let list = LinkedList();
    loop 1000 {
        list.concat($);
    }
    list.print();

    if list.contains(500) {
        io::println("List contains 500");
    } else {
        io::println("List does not contain 500");
    }

    if list.contains(2500) {
        io::println("List contains 2500");
    } else {
        io::println("List does not contain 2500");
    }

    list.free();

}
```
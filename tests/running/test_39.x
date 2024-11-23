// Simple linked list implementation

import "../../lib/std.x" as std;

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
		outI64(curr->val);
		curr = curr->next;
	}
	outStr("Size is: ");
	outI64(list->len);
}

fn freeLinkedList(list: LinkedList*) -> void {
	let mut curr: Node* = list->head;
	while curr != null {
		let mut next: Node* = curr->next;
		std::free(curr as void*);
		curr = next;
	}
	std::free(list as void*);
}


fn main() -> i64 {

	let list: LinkedList* = makeLinkedList();
	loop 1000 {
		addNumToLinkedList(list, $);
	}
	printLinkedList(list);

	freeLinkedList(list);

}
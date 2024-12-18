using std, io;

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
		free(curr);
		curr = next;
	}
	free(list);
}

fn LinkedList() -> LinkedList* {
	let mut val: LinkedList* = malloc(size(LinkedList));
	val->len = 0;
	val->head = null;
	return val;
}

fn Node(val: i64) -> Node* {
	let mut newNode: Node* = malloc(size(Node));
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
		println(curr->val);
		curr = curr->next;
	}
	print("Size is: ");
	println(list->len);
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

	let mut list = LinkedList();
	loop 1000 {
		list.concat($);
	}
	list.print();

	if list.contains(500) {
		println("List contains 500");
	} else {
		println("List does not contain 500");
	}

	if list.contains(2500) {
		println("List contains 2500");
	} else {
		println("List does not contain 2500");
	}

    list.free();

}

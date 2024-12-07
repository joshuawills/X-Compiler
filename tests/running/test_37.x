// Accessing struct pointers at the top level

import std, io;

struct Node -> {
	mut value: i64
}

struct NodeInNode -> {
	mut node: Node
}

fn main() -> void {

	let mut node = Node { 10 };

	let nodePtr = &node;

	nodePtr->value = 20;

	io::println(node.value);
	io::println(nodePtr->value);


	let mut nodeInNode = NodeInNode { Node { 30 } };
	let nodeInNodePtr = &nodeInNode;

	nodeInNodePtr->node.value = 40;

	io::println(nodeInNode.node.value);
	io::println(nodeInNodePtr->node.value);


}

// Accessing struct pointers at the top level

import std;

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

	std::println(node.value);
	std::println(nodePtr->value);


	let mut nodeInNode = NodeInNode { Node { 30 } };
	let nodeInNodePtr = &nodeInNode;

	nodeInNodePtr->node.value = 40;

	std::println(nodeInNode.node.value);
	std::println(nodeInNodePtr->node.value);


}

// Passing struct pointers to functions

import std, io;

struct Node -> {
	mut value: i64
}

struct NodeInNode -> {
	mut node: Node
}

fn setNodeValueTo42(node: Node*) -> void {
	node->value = 42;
}

fn setNodeInNodeValueTo42(node: NodeInNode*) -> void {
	node->node.value = 42;
}

fn main() -> void {

	let mut node = Node { 21 };
	io::println(node.value);
	setNodeValueTo42(&node);
	io::println(node.value);

	let mut nodeInNode = NodeInNode { Node { 21 } };
	io::println(nodeInNode.node.value);
	setNodeInNodeValueTo42(&nodeInNode);
	io::println(nodeInNode.node.value);

}

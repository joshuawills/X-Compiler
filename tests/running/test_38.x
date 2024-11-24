// Passing struct pointers to functions

import "../../lib/std.x" as std;

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
	outI64(node.value);
	setNodeValueTo42(&node);
	outI64(node.value);

	let mut nodeInNode = NodeInNode { Node { 21 } };
	outI64(nodeInNode.node.value);
	setNodeInNodeValueTo42(&nodeInNode);
	outI64(nodeInNode.node.value);

}

// Accessing struct pointers at the top level

import "../../lib/std.x" as std;

struct Node -> {
	mut value: i64
}

struct NodeInNode -> {
	mut node: Node
}

fn main() -> i64 {

	let mut node = Node { 10 };

	let nodePtr = &node;

	nodePtr->value = 20;

	outI64(node.value);
	outI64(nodePtr->value);


	let mut nodeInNode = NodeInNode { Node { 30 } };
	let nodeInNodePtr = &nodeInNode;

	nodeInNodePtr->node.value = 40;

	outI64(nodeInNode.node.value);
	outI64(nodeInNodePtr->node.value);

	return 0;

}

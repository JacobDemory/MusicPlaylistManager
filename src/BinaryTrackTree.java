public class BinaryTrackTree {
	TreeNode root;
	BinaryTrackTree() 
	{ 
		root = null; 
	}

	BinaryTrackTree(Track value) { 
		root = new TreeNode(value); 
	}

	void insert(Track track) { 
		root = insertHelper(root, track); 
	}

	TreeNode insertHelper(TreeNode node, Track key)
	{
		if (node == null) {
			node = new TreeNode(key);
			return node;
		}

		if (key.getTitle().compareTo(node.key.getTitle()) < 0)
			node.left = insertHelper(node.left, key);
		else if (key.getTitle().compareTo(node.key.getTitle()) > 0)
			node.right = insertHelper(node.right, key);
		return node;
	}

	public TreeNode find(TreeNode node, String title)
	{
		if (node == null || node.key.getTitle().equalsIgnoreCase(title))
			return node;

		if (node.key.getTitle().compareTo(title) < 0)
			return find(node.right, title);

		return find(node.left,title);
	}
}

class TreeNode {
	Track key;
	TreeNode left, right;

	public TreeNode(Track t)
	{
		key = t;
		left = right = null;
	}
}


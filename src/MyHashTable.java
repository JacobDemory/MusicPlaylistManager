import java.util.ArrayList;
import java.util.Objects;

public class MyHashTable<K, V> {
	private int size;
	private ArrayList<Node<K, V> > buckets;
	private int bucketCount;

	public MyHashTable()
	{
		buckets = new ArrayList<>();
		bucketCount = 10;
		size = 0;

		for (int i = 0; i < bucketCount; i++)
			buckets.add(null);
	}

	public int size() {
		return size;
	}

	public boolean isEmpty() {
		return size() == 0;
	}

	private final int getHash (K key) {
		return Objects.hashCode(key);
	}

	private int getIndex(K key)
	{
		int hash = getHash(key);
		int id = hash % bucketCount;
		id = id < 0 ? id * -1 : id;
		return id;
	}

	public V remove(K key)
	{
		int bucketId = getIndex(key);
		int hash = getHash(key);
		Node<K, V> topNode = buckets.get(bucketId);
		Node<K, V> prevNode = null;

		while (topNode != null) {
			if (topNode.key.equals(key) && hash == topNode.hash)
				break;

			prevNode = topNode;
			topNode = topNode.next;
		}

		if (topNode == null)
			return null;

		size--;

		if (prevNode != null)
			prevNode.next = topNode.next;
		else
			buckets.set(bucketId, topNode.next);

		return topNode.value;
	}

	public V get(K key)
	{
		int bucketId = getIndex(key);
		int hash = getHash(key);

		Node<K, V> topNode = buckets.get(bucketId);

		while (topNode != null) {
			if (topNode.key.equals(key) && topNode.hash == hash)
				return topNode.value;
			topNode = topNode.next;
		}

		return null;
	}

	public void add(K key, V value)
	{
		int bucketId = getIndex(key);
		int hash = getHash(key);
		Node<K, V> topNode = buckets.get(bucketId);

		while (topNode != null) {
			if (topNode.key.equals(key) && topNode.hash == hash) {
				topNode.value = value;
				return;
			}
			topNode = topNode.next;
		}

		size++;
		topNode = buckets.get(bucketId);
		Node<K, V> tempNode
				= new Node<K, V>(key, value, hash);
		tempNode.next = topNode;
		buckets.set(bucketId, tempNode);

		if ((1.0 * size) / bucketCount >= 0.7) {
			ArrayList<Node<K, V> > tempBucket = buckets;
			buckets = new ArrayList<>();
			bucketCount = 2 * bucketCount;
			size = 0;
			for (int i = 0; i < bucketCount; i++)
				buckets.add(null);

			for (Node<K, V> headNode : tempBucket) {
				while (headNode != null) {
					add(headNode.key, headNode.value);
					headNode = headNode.next;
				}
			}
		}
	}
}

class Node<K, V> {
	K key;
	V value;
	final int hash;
	Node<K, V> next;

	public Node(K key, V value, int hash)
	{
		this.key = key;
		this.value = value;
		this.hash = hash;
	}
}
package edu.yu.cs.com1320.project.impl;

import java.io.IOException;
import java.io.UncheckedIOException;

import edu.yu.cs.com1320.project.BTree;
import edu.yu.cs.com1320.project.stage5.PersistenceManager;

public class BTreeImpl<Key extends Comparable<Key>, Value> implements BTree<Key, Value>
{
	private static final int MAX = 4;
	private Node root = new Node(0);
	private int height;

	private static final class Node
	{
		private int entryCount;
		private Entry[] entries = new Entry[BTreeImpl.MAX];
		private Node next;

		private Node(int k)
		{
			this.entryCount = k;
		}

		private void setNext(Node next)
		{
			this.next = next;
		}

		private Node getNext()
		{
			return this.next;
		}
	}

	private static class Entry
	{
		@SuppressWarnings("rawtypes")
		private Comparable key;
		private Object val;
		private Node child;
		private boolean onDisk= false;

		@SuppressWarnings("rawtypes")
		private Entry(Comparable key, Object val, Node child)
		{
			this.key = key;
			this.val = val;
			this.child = child;
		}

		private Object getValue()
		{
			return this.val;
		}

		private void setValue(Object value)
		{
			this.val= value;
		}
		
		private void setToOnDisk()
		{
			if(onDisk == true)
			{
				throw new IllegalAccessError();
			}
			this.onDisk= true;
		}
	}	
	
	private PersistenceManager<Key, Value> persistenceManager;

	@SuppressWarnings("unchecked")
	@Override
	public Value get(Key k)
	{
		if (k == null)
		{
			throw new IllegalArgumentException("key is null");
		}
		Entry entry = this.get(this.root, k, this.height);
		if(entry != null)
		{
			if(entry.onDisk == true)
			{
				moveToMemmory(k, entry);
			}
			return (Value) entry.val;
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Value put(Key k, Value v)
	{
		if (k == null)
		{
			throw new IllegalArgumentException("key is null");
		}
		Entry alreadyThere = this.get(this.root, k, this.height);
		if (alreadyThere != null)
		{
			if(alreadyThere.onDisk == true)
			{
				moveToMemmory(k, alreadyThere);
			}
			Value oldVal= (Value) alreadyThere.getValue();
			alreadyThere.val = v;
			return oldVal;
		}

		Node newNode = this.put(this.root, k, v, this.height);
		if (newNode == null)
		{
			return null;
		}
		Node newRoot = new Node(2);
		newRoot.entries[0] = new Entry(this.root.entries[0].key, null, this.root);
		newRoot.entries[1] = new Entry(newNode.entries[0].key, null, newNode);
		this.root = newRoot;
		this.height++;
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void moveToDisk(Key k) throws Exception
	{
		if(k == null)
		{
			throw new IllegalArgumentException("key is null");
		}
		if(persistenceManager == null)
		{
			throw new IllegalAccessError("persistence manager was not set");
		}
		Entry entry= get(root, k, height);
		if(entry == null || entry.getValue() == null)
		{
			throw new IllegalArgumentException("key does not exist :"+k);
		}
		persistenceManager.serialize(k, (Value) entry.getValue());
		entry.setToOnDisk();
		entry.setValue(null);
	}

	@Override
	public void setPersistenceManager(PersistenceManager<Key, Value> pm)
	{
		this.persistenceManager= pm;
	}
	
	private Value moveToMemmory(Key k, Entry entry)
	{
		if(entry.onDisk != true)
		{
			throw new IllegalArgumentException();
		}
		Value value;
		try
		{
			value = persistenceManager.deserialize(k);
			persistenceManager.delete(k);
			entry.onDisk= false;
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
		entry.setValue(value);
		return value;
	}
	
	private Entry get(Node currentNode, Key key, int height)
	{
		Entry[] entries = currentNode.entries;

		if (height == 0)
		{
			for (int j = 0; j < currentNode.entryCount; j++)
			{
				if (isEqual(key, entries[j].key))
				{
					return entries[j];
				}
			}
			return null;
		}
		else
		{
			for (int j = 0; j < currentNode.entryCount; j++)
			{
				if (j + 1 == currentNode.entryCount || less(key, entries[j + 1].key))
				{
					return this.get(entries[j].child, key, height - 1);
				}
			}
			return null;
		}
	}
	
	private Node put(Node currentNode, Key key, Value val, int height)
	{
		int j;
		Entry newEntry = new Entry(key, val, null);

		if (height == 0)
		{
			
			for (j = 0; j < currentNode.entryCount; j++)
			{
				if (less(key, currentNode.entries[j].key))
				{
					break;
				}
			}
		}
		else
		{
			for (j = 0; j < currentNode.entryCount; j++)
			{
				if ((j + 1 == currentNode.entryCount) || less(key, currentNode.entries[j + 1].key))
				{
					Node newNode = this.put(currentNode.entries[j++].child, key, val, height - 1);
					if (newNode == null)
					{
						return null;
					}
					newEntry.key = newNode.entries[0].key;
					newEntry.val = null;
					newEntry.child = newNode;
					break;
				}
			}
		}
		for (int i = currentNode.entryCount; i > j; i--)
		{
			currentNode.entries[i] = currentNode.entries[i - 1];
		}
		currentNode.entries[j] = newEntry;
		currentNode.entryCount++;
		if (currentNode.entryCount < BTreeImpl.MAX)
		{
			return null;
		}
		else
		{
			return this.split(currentNode, height);
		}
	}
	
	private Node split(Node currentNode, int height)
	{
		Node newNode = new Node(BTreeImpl.MAX / 2);
		currentNode.entryCount = BTreeImpl.MAX / 2;
		for (int j = 0; j < BTreeImpl.MAX / 2; j++)
		{
			newNode.entries[j] = currentNode.entries[BTreeImpl.MAX / 2 + j];
		}
		if (height == 0)
		{
			newNode.setNext(currentNode.getNext());
			currentNode.setNext(newNode);
		}
		return newNode;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static boolean less(Comparable k1, Comparable k2)
	{
		return k1.compareTo(k2) < 0;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static boolean isEqual(Comparable k1, Comparable k2)
	{
		return k1.compareTo(k2) == 0;
	}
}
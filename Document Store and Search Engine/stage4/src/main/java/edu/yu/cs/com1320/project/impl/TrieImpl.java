package edu.yu.cs.com1320.project.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.yu.cs.com1320.project.Trie;

public class TrieImpl<Value> implements Trie<Value>
{
	private static class Node
	{
		private final Node parent;
		private final char index;
		private final Node[] childArray= new Node[36];
		private final Set<Object> values= new HashSet<>();
		
		private Node(Node parent, char index)
		{
			this.parent= parent;
			this.index= index;
		}
		
		private void addValue(Object value)
		{
			values.add(value);
		}
		
		private void insertChild(char letter, Node child)
		{
			int index= getIndex(letter);
			childArray[index]= child;
		}
		
		private static int getIndex(char letter)
		{
			if(!Character.isLetterOrDigit(letter))
			{
				throw new IllegalArgumentException("( "+letter+" ) is not a valid letter or digit");
			}
			
			if(Character.isLetter(letter))
			{
				return (int)letter-87;
			}
			return (int) letter-48;
		}

		private Node getNodeAt(char letter)
		{
			int index= getIndex(letter);
			return childArray[index];
		}
		
		private Set<Node> getChildren()
		{
			Set<Node> set= new HashSet<>();
			for(Node child: childArray)
			{
				if(child != null)
				{
					set.add(child);
				}
			}
			return set;
		}
		
		private Boolean hasChildren()
		{
			for(Node child: childArray)
			{
				if(child != null)
				{
					return true;
				}
			}
			return false;
		}
	}
	
	private final Node root= new Node(null, '*');
	
	@Override
	public void put(String key, Value val)
	{
		if(val == null || key == null)
		{
			throw new IllegalArgumentException("key or val is null");
		}
		key= key.toLowerCase();
		put(key, val, root);
	}
	
	private void put(String key, Value val, Node node)
	{
		if(key.isEmpty())
		{
			node.addValue(val);
			return;
		}
		
		char currentLetter= key.charAt(0);		
		Node nextNode= node.getNodeAt(currentLetter);
		if(nextNode == null)
		{
			nextNode= new Node(node, currentLetter);
			node.insertChild(currentLetter, nextNode);
		}
		put(key.substring(1), val, nextNode);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Value> getAllSorted(String key, Comparator<Value> comparator)
	{
		key= key.toLowerCase();
		Node node= getNode(key, root, 0);
		if(node == null)
		{
			return new ArrayList<>();
		}
		List<Value> list= new ArrayList<>((Set<Value>) node.values);
		list.sort(comparator);
		return list;
	}

	@Override
	public List<Value> getAllWithPrefixSorted(String prefix, Comparator<Value> comparator)
	{
		prefix= prefix.toLowerCase();
		Node rootOfSubtree= getNode(prefix, root, 0);
		Set<Value> set= new HashSet<>();
		getValuesInSubtree(rootOfSubtree, set);
		List<Value> list= new ArrayList<>(set);
		list.sort(comparator);
		return list;
	}
	
	private Node getNode(String key, Node node, int index)
	{
		if(index == key.length())
		{
			return node;
		}
		
		Node nextNode= node.getNodeAt(key.charAt(index));
		if(nextNode == null)
		{
			return null;
		}
		return getNode(key, nextNode, ++index);
	}
	
	@SuppressWarnings("unchecked")
	private void getValuesInSubtree(Node node, Collection<Value> collection)
	{
		if(node == null)
		{
			return;
		}
		if(!node.values.isEmpty())
		{
			for(Object value: node.values)
			{
				collection.add((Value)value);
			}
		}
		
		for(Node child :node.getChildren())
		{
			getValuesInSubtree(child, collection);
		}
	}

	@Override
	public Set<Value> deleteAllWithPrefix(String prefix)
	{
		prefix= prefix.toLowerCase();
		Node subRoot= getNode(prefix, root, 0);
		Set<Value> set= new HashSet<>();
		getValuesInSubtree(subRoot, set);
		if(deleteNode(subRoot))
		{
			cleanUpDelete(subRoot);
		}
		return set;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Set<Value> deleteAll(String key)
	{
		key= key.toLowerCase();
		Node node= getNode(key, root, 0);
		if(node == null)
		{
			return new HashSet<>();
		}
		Set<Value> deleted= new HashSet(node.values);
		node.values.clear();
		cleanUpDelete(node);
		return deleted;
	}

	@Override
	public Value delete(String key, Value val)
	{
		return delete(key.toLowerCase(), val, root);
	}

	private Value delete(String key, Value val, Node node)
	{
		if(key.isEmpty())
		{
			if(node.values.contains(val))
			{
				node.values.remove(val);
				cleanUpDelete(node);
				return val;
			}
			return null;
		}
		
		char currentLetter= key.charAt(0);
		Node nextNode= node.getNodeAt(currentLetter);
		if(nextNode == null)
		{
			return null;
		}
		Value oldValue= delete(key.substring(1), val, nextNode);
		return oldValue;
	}
	
	private void cleanUpDelete(Node node)
	{
		if(node == null)
		{
			throw new IllegalArgumentException();
		}
		if(node.values.isEmpty() && !node.hasChildren())
		{
			if(deleteNode(node))
			{
				cleanUpDelete(node.parent);
			}
		}
	}
	
	private boolean deleteNode(Node node)
	{
		if(node == null)
		{
			return false;
		}
		Node parent = node.parent;
		if(parent == null)
		{
			return false;
		}
		parent.insertChild(node.index, null);
		return true;
	}
}
package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.HashTable;

public class HashTableImpl<Key, Value> implements HashTable<Key, Value>
{
	private class Current<Key, Value>
	{
		private Key key;
		private Value value;
		Current<Key, Value> next;
		
		private Current(Key key, Value value)
		{
			if(key == null)
			{
				throw new IllegalArgumentException();
			}
			this.key=key;
			this.value=value;
		}
	}
	private Current<?,?> [] table;
	private int tableSize=5;
	
	public HashTableImpl()
	{
		table= new Current[tableSize];
	}
	
	@Override
	public Value get(Key k)
	{
		if(k==null)
		{
			throw new IllegalArgumentException();
		}
		
		Current current=table[getIndex(k)];
		while(current != null && !k.equals((Key)current.key))
		{
			current= current.next;
		}
		if(current == null)
		{
			return null;
		}
		return (Value)current.value;
	}

	@Override
	public Value put(Key k, Value v)
	{
		int index= getIndex(k);
		Current current= table[index];
		if(current == null)
		{
			table[index]= new Current<Key,Value>(k, v);
			return null;
		}
		while(true)
		{
			if(current == null)
			{
				Current next= table[index];
				table[index]= new Current<Key,Value>(k, v);
				table[index].next= next;
				return null;
			}
			if(k.equals((Key)current.key))
			{
				Value val= (Value)current.value;
				current.value= v;
				return val;
			}
			current= current.next;
		}
	}
	
	private int getIndex(Object o)
	{
		return o.hashCode()%tableSize;
	}
}
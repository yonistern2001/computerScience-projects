package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.HashTable;

public class HashTableImpl<Key, Value> implements HashTable<Key, Value>
{
	private class Current<Key, Value>
	{
		private Key key;
		private Value value;
		private Current<Key, Value> next;
		
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
	private int arrayElements=0;
	private int sizeToRe_hash=3;
	
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
		if(v == null)
		{
			return delete(k, v);
		}
		Current current= table[getIndex(k)];
		if(current == null)
		{
			arrayElements++;
			if(arrayElements == sizeToRe_hash)
			{
				doubleArray();
			}
			table[getIndex(k)]= new Current<Key,Value>(k, v);
			return null;
		}
		return put(k, v, getIndex(k), current);
	}

	private Value put(Key k, Value v, int index, Current current)
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
		return put(k, v, index, current.next);
	}
	
	private Value delete(Key k, Value v)
	{
		Current current= table[getIndex(k)];
		if(current == null)
		{
			return null;
		}
		Key key= (Key) current.key;
		if(key.equals(k))
		{
			table[getIndex(k)]= current.next;
			arrayElements--;
			return (Value) current.value;
		}
		Current previous= null;
		while(!key.equals(k))
		{
			previous= current;
			current= current.next;
			if(current == null)
			{
				return null;
			}
			key= (Key) current.key;
		}
		previous.next= current.next;
		return (Value) current.value;
	}

	private int getIndex(Object o)
	{
		return o.hashCode()%tableSize;
	}
	
	private void doubleArray()
	{
		arrayElements= 0;
		tableSize*=2;
		sizeToRe_hash= (tableSize*7)/10;
		Current<?,?>[] oldArray= table;
		table= new Current[tableSize];
		for(Current cur: oldArray)
		{
			re_hash(cur);
		}
	}

	private void re_hash(Current cur)
	{
		if(cur == null)
		{
			return;
		}
		this.put((Key)cur.key, (Value)cur.value);
		re_hash(cur.next);
	}
}
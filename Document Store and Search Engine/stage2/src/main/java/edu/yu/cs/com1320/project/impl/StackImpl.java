package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.Stack;

public class StackImpl<T> implements Stack<T>
{
	private int size=0;
	private Link head;
	
	private class Link
	{
		private final T input;
		private Link next;
		
		private Link(T input)
		{
			this.input= input;
		}
	}

	@Override
	public void push(T element)
	{
		Link link= new Link(element);
		link.next= head;
		head= link;
		size++;
	}

	@Override
	public T pop()
	{
		if(head == null)
		{
			return null;
		}
		Link link= head;
		this.head= link.next;
		size--;
		return link.input;
	}

	@Override
	public T peek()
	{
		if(head == null)
		{
			return null;
		}
		return head.input;
	}

	@Override
	public int size()
	{
		return size;
	}
}

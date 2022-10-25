package edu.yu.cs.com1320.project.impl;

import java.util.Comparator;
import java.util.NoSuchElementException;

import edu.yu.cs.com1320.project.MinHeap;

public class MinHeapImpl<E extends Comparable<E>> extends MinHeap<E>
{
	private static final int STARTING_SIZE = 4;
	private Comparator<E> comparator;
	
	@SuppressWarnings("unchecked")
	public MinHeapImpl()
	{
		elements= (E[]) new Comparable[STARTING_SIZE];
	}
	
	public MinHeapImpl(Comparator<E> comparator)
	{
		this();
		this.comparator= comparator;
	}

	@Override
	public void reHeapify(E element)
	{
		int index= getArrayIndex(element);
		if(index == -1)
		{
			throw new NoSuchElementException("the element passed in is not in the heap");
		}
		super.upHeap(index);
		super.downHeap(index);
	}
	
	@Override
	protected int getArrayIndex(E element)
	{
		for(int i=0; i < super.elements.length; i++)
		{
			if(element.equals(super.elements[i]))
			{
				return i;
			}
		}
		return -1;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void doubleArraySize()
	{
		int oldLength= super.elements.length;
		int size= oldLength*2;
		E[] temp= (E[]) new Comparable[size];
		for(int index=0; index < oldLength; index++)
		{
			temp[index]= elements[index];
		}
		elements= temp;
	}
	
	@Override
	protected boolean isGreater(int i, int j)
	{
		if(comparator == null)
		{
			return super.isGreater(i, j);
		}
		return this.comparator.compare(this.elements[i], this.elements[j]) > 0;
	}
}
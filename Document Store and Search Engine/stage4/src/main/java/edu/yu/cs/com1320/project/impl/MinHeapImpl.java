package edu.yu.cs.com1320.project.impl;

import java.util.NoSuchElementException;

import edu.yu.cs.com1320.project.MinHeap;

public class MinHeapImpl<E extends Comparable<E>> extends MinHeap<E>
{
	private static final int STARTING_SIZE = 4;
	
	@SuppressWarnings("unchecked")
	public MinHeapImpl()
	{
		elements= (E[]) new Comparable[STARTING_SIZE];
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

//	@Override
//	protected int getArrayIndex(E element)
//	{
//		return compare(element, 1);
//	}
//
//	private int getArrayIndex(E element, int indexOfElement)
//	{
//		int right= getRightChildIndex(indexOfElement);
//		int left= getLeftChildIndex(indexOfElement);
//		int output= compare(element, right);
//		if(output != -1)
//		{
//			return output;
//		}
//		return compare(element, left);
//	}
//
//	private int compare(E element, int index)
//	{
//		if(index > super.count)
//		{
//			return -1;
//		}
//		int comparison= element.compareTo(super.elements[index]);
//		if(comparison == 0)
//		{
//			return index;
//		}
//		if(comparison > 0)
//		{
//			return getArrayIndex(element, index);
//		}
//		return -1;
//	}
//	
//	private static int getRightChildIndex(int index)
//	{
//		return 2*index;
//	}
//	
//	private static int getLeftChildIndex(int index)
//	{
//		return 2*index+1;
//	}
}
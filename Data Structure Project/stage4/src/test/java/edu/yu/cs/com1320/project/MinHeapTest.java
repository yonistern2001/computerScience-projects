package edu.yu.cs.com1320.project;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Objects;

import org.junit.jupiter.api.Test;

import edu.yu.cs.com1320.project.impl.MinHeapImpl;

class MinHeapTest
{
	private final MinHeap<Int> heap= new MinHeapImpl<>();
	
	@Test
	void insertAndRemove()
	{
		heap.insert(new Int(2));
		heap.insert(new Int(3));
		heap.insert(new Int(10));
		heap.insert(new Int(1));
		heap.insert(new Int(4));
		assertTrue(!heap.isEmpty());
		assertEquals(new Int(1), heap.remove());
		assertEquals(new Int(2), heap.remove());
		assertEquals(new Int(3), heap.remove());
		assertEquals(new Int(4), heap.remove());
		assertEquals(new Int(10), heap.remove());
		assertTrue(heap.isEmpty());
	}

	@Test
	void getElementByIndex()
	{
		Int int10 = new Int(10);
		heap.insert(new Int(2));
		heap.insert(new Int(3));
		heap.insert(int10);
		heap.insert(new Int(1));
		heap.insert(new Int(4));
		assertEquals(1, heap.getArrayIndex(new Int(1)));
		assertEquals(3, heap.getArrayIndex(int10));
		heap.insert(new Int(12));
		heap.insert(new Int(122));
		heap.insert(new Int(112));
		heap.insert(new Int(1123));
		heap.insert(new Int(9));
		heap.insert(new Int(0));
		assertEquals(1, heap.getArrayIndex(new Int(0)));
		assertEquals(9, heap.getArrayIndex(new Int(1123)));
		assertEquals(8, heap.getArrayIndex(new Int(112)));
		assertEquals(10, heap.getArrayIndex(new Int(9)));
		assertEquals(11, heap.getArrayIndex(new Int(4)));
		assertEquals(6, heap.getArrayIndex(new Int(12)));
	}
	
	@Test
	void reHeapify()
	{
		Int int1= new Int(1);
		Int int2= new Int(2);
		Int int3= new Int(3);
		Int int4= new Int(4);
		Int int5= new Int(5);
		heap.insert(int5);
		heap.insert(int4);
		heap.insert(int3);
		heap.insert(int2);
		heap.insert(int1);
		
		int1.setValue(5);
		heap.reHeapify(int1);
		int2.setValue(4);
		heap.reHeapify(int2);
		int3.setValue(3);
		heap.reHeapify(int3);
		int4.setValue(2);
		heap.reHeapify(int4);
		int5.setValue(1);
		heap.reHeapify(int5);
		
		assertEquals(int5, heap.remove());
		assertEquals(int4, heap.remove());
		assertEquals(int3, heap.remove());
		assertEquals(int2, heap.remove());
		assertEquals(int1, heap.remove());
	}
	
	private static class Int implements Comparable<Int>
	{
		private int value;

		public Int(int i)
		{
			this.value = i;
		}

		@SuppressWarnings("unused")
		private int getValue()
		{
			return value;
		}

		private void setValue(int i)
		{
			this.value = i;
		}

		@Override
		public int compareTo(Int o)
		{
			return Integer.compare(value, o.value);
		}

		@Override
		public int hashCode() {
			return Objects.hash(value);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Int other = (Int) obj;
			return value == other.value;
		}
	}
}

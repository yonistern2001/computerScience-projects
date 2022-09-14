import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Comparator;

import org.junit.jupiter.api.Test;

import edu.yu.cs.com1320.project.impl.MinHeapImpl;

class MinHeapTest
{
	@Test
	void insertWithoutComparitor()
	{
		MinHeapImpl<Integer> heap = new MinHeapImpl<>();
		heap.insert(2);
		heap.insert(1);
		heap.insert(12);
		heap.insert(100);
		
		assertEquals(1, heap.remove());
		assertEquals(2, heap.remove());
		assertEquals(12, heap.remove());
		assertEquals(100, heap.remove());
	}
	
	@Test
	void insertWithComparitor()
	{
		MinHeapImpl<Integer> heap = new MinHeapImpl<>(new Comparator<Integer>()
		{
			@Override
			public int compare(Integer o1, Integer o2)
			{
				return o2-o1;
			}
		});
		heap.insert(2);
		heap.insert(1);
		heap.insert(12);
		heap.insert(100);
		
		assertEquals(100, heap.remove());
		assertEquals(12, heap.remove());
		assertEquals(2, heap.remove());
		assertEquals(1, heap.remove());
	}
}

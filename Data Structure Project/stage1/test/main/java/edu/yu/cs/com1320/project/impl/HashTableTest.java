package edu.yu.cs.com1320.project.impl;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class HashTableTest
{
	private final HashTableImpl<Integer, String> table= new HashTableImpl<>();
	
	@Test
	public void get_nothing() 
	{
		assertNull(table.get(1));	
	}
	
   @Test
	void put()
	{	
		table.put(1, "A");
		assertEquals("A", table.get(1));
		table.put(3, "C");
		assertEquals("C", table.get(3));
		assertEquals("A", table.get(1));
	}

    @Test
  	void put_replace()
  	{	
        assertNull(table.put(1, "A"));
        assertEquals("A", table.put(1, "C"));
  		assertEquals("C", table.get(1));
  	}
   
	@Test
	void putMany()
	{
		table.put(1, "A");
		assertEquals("A", table.get(1));
		assertEquals("A", table.put(1, "B"));
		assertEquals("B", table.get(1));
		table.put(2, "B");
		table.put(3, "C");
		table.put(4, "D");
		table.put(5, "E");
		table.put(6, "F");
		table.put(7, "G");
		assertEquals("B", table.get(2));
		assertEquals("C", table.get(3));
		assertEquals("D", table.get(4));
		assertEquals("E", table.get(5));
		assertEquals("F", table.get(6));
		assertEquals("G", table.get(7));
	}
	
	@Test
	void replacingValues()
	{
		table.put(1, "A");
		table.put(2, "B");
		assertEquals("A", table.put(1, "B"));
		assertEquals("B", table.get(1));
		assertEquals(null, table.put(4, "abc"));
		assertEquals("B", table.put(1, "Z"));
		assertEquals("Z", table.get(1));
		assertEquals("B", table.put(2, "2"));
		assertEquals("2", table.get(2));
	}
	
	@Test
	void testWithCollisions()
	{
		table.put(1, "A");
		assertEquals("A", table.get(1));
		table.put(2, "B");
		table.put(3, "C");
		table.put(4, "D");
		table.put(5, "E");
		table.put(6, "F");
		table.put(7, "G");
		assertEquals("B", table.get(2));
		assertEquals("C", table.get(3));
		assertEquals("D", table.get(4));
		assertEquals("E", table.get(5));
		assertEquals("F", table.get(6));
		assertEquals("G", table.get(7));
		
		assertEquals("E", table.put(5, "B"));
		assertEquals("B", table.get(5));
		
		assertEquals(null, table.put(11, "B"));
		assertEquals("B", table.get(11));
	}
	
	@Test
	void getValueThatDoesntExist()
	{
		HashTableImpl<Integer, String> table= new HashTableImpl<>();
		assertEquals(null, table.get(1));
		table.put(1, "A");
		assertEquals(null, table.get(10));
		assertEquals(null, table.get(1550));
	}
}
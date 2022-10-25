
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import edu.yu.cs.com1320.project.impl.HashTableImpl;

public class HashTableTest
{
	private final HashTableImpl<Integer, String> table= new HashTableImpl<>();
	
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
		table.put(2, "Z");
	}

    @Test
  	void put_replace()
  	{	
        assertNull(table.put(1, "A"));
        assertEquals("A", table.put(1, "C"));
  		assertEquals("C", table.get(1));
  	}
    
    @Test
    public void testGet() {
        HashTableImpl<String, String> table2 = new HashTableImpl<>();
        table2.put("Key1", "Value1");
        table2.put("Key2","Value2");
        table2.put("Key3","Value3");
        table2.put("Key4","Value4");
        table2.put("Key5","Value5");
        table2.put("Key6","Value6");
        assertEquals("Value1",table2.get("Key1"));
        assertEquals("Value2",table2.get("Key2"));
        assertEquals("Value3",table2.get("Key3"));
        assertEquals("Value4",table2.get("Key4"));
        
        assertEquals("Value6",table2.get("Key6"));

        assertEquals("Value5",table2.get("Key5"));
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
	
	@Test
	void deleteKeys()
	{
		table.put(1, "A");
		table.put(2, "B");
		table.put(3, "C");
		table.put(4, "D");
		table.put(5, "E");
		table.put(6, "F");
		table.put(7, "G");
		assertEquals("A", table.put(1, null));
		assertEquals("B", table.put(2, null));
		assertEquals("C", table.put(3, null));
		assertEquals("D", table.put(4, null));
		assertEquals("E", table.put(5, null));
		assertEquals("F", table.put(6, null));
		assertEquals("G", table.put(7, null));
		assertEquals(null, table.get(1));
		assertEquals(null, table.get(2));
		assertEquals(null, table.get(3));
		assertEquals(null, table.get(4));
		assertEquals(null, table.get(5));
		assertEquals(null, table.get(6));
		assertEquals(null, table.get(6));
	}
}
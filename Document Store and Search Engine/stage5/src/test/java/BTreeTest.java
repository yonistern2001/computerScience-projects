import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.yu.cs.com1320.project.BTree;
import edu.yu.cs.com1320.project.impl.BTreeImpl;
import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.impl.DocumentImpl;
import edu.yu.cs.com1320.project.stage5.impl.DocumentPersistenceManager;

public class BTreeTest
{
	private final BTree<URI, Document> table= new BTreeImpl<>();
	URI uri1= createUri("http://1"), uri2= createUri("http://2"), uri3= createUri("http://3"), uri4= createUri("http://4");
	Document doc1= createDoc(uri1, "doc1"), doc2= createDoc(uri2, "doc2"), doc3= createDoc(uri3, "doc3"), doc4= createDoc(uri4, "doc4");
	
	@BeforeEach
	public void before()
	{
		table.setPersistenceManager(new DocumentPersistenceManager(null));
	}
	
	@Test
	public void get_nothing()
	{
		assertNull(table.get(createUri("1")));
	}

	@Test
	void put()
	{
		table.put(uri1, doc1);
		assertEquals(doc1, table.get(uri1));
		table.put(uri3, doc3);
		assertEquals(doc3, table.get(uri3));
		assertEquals(doc1, table.get(uri1));
	}

	@Test
	void put_replace()
	{
		assertNull(table.put(uri1, doc1));
		assertEquals(doc1, table.put(uri1, doc3));
		assertEquals(doc3, table.get(uri1));
	}

	@Test
	void putMany()
	{
		table.put(uri1, doc1);
		assertEquals(doc1, table.get(uri1));
		assertEquals(doc1, table.put(uri1, doc2));
		assertEquals(doc2, table.get(uri1));
		table.put(uri2, doc2);
		table.put(uri3, doc3);
		table.put(uri4, doc4);
		assertEquals(doc2, table.get(uri2));
		assertEquals(doc3, table.get(uri3));
		assertEquals(doc4, table.get(uri4));
	}
	
	@Test
	void getWithNonExistantKey()
	{
		table.put(uri1, doc1);
		assertNull(table.get(uri2));
	}
	
	@Test
	void putAndMoveToDisk() throws Exception
	{
		table.put(uri1, doc1);
		table.put(uri2, doc2);
		table.put(uri3, doc3);
		table.moveToDisk(uri1);
		assertEquals(doc1, table.get(uri1));
		table.moveToDisk(uri2);
		assertEquals(doc2, table.put(uri2, doc4));
		table.moveToDisk(uri2);
		assertEquals(doc4, table.get(uri2));
	}
	
	@Test
	void moveToDiskWithInvalidKey() throws Exception
	{
		try
		{
			table.moveToDisk(uri1);
			fail();
		}
		catch(IllegalArgumentException e)
		{
			
		}
	}
	
	@Test
	void moveToDiskWithSameUri() throws Exception
	{
		table.put(uri1, doc1);
		table.moveToDisk(uri1);
		assertEquals(doc1, table.put(uri1, doc2));
		table.moveToDisk(uri1);
		assertEquals(doc2, table.get(uri1));
		table.moveToDisk(uri1);
		assertEquals(doc2, table.put(uri1, doc2));
	}
	
	@Test
	void replacingValues()
	{
		table.put(uri1, doc1);
		table.put(uri2, doc2);
		assertEquals(doc1, table.put(uri1, doc2));
		assertEquals(doc2, table.get(uri1));
		assertEquals(null, table.put(uri4, doc4));
		assertEquals(doc2, table.put(uri1, doc3));
		assertEquals(doc3, table.get(uri1));
		assertEquals(doc2, table.put(uri2, doc2));
		assertEquals(doc2, table.get(uri2));
	}
	
	@Test
	void getValueThatDoesntExist()
	{
		assertEquals(null, table.get(uri3));
		table.put(uri2, doc2);
		assertEquals(null, table.get(uri1));
		assertEquals(null, table.get(uri4));
	}

	@Test
	void deleteKeys()
	{
		table.put(uri1, doc1);
		table.put(uri2, doc2);
		table.put(uri3, doc3);
		table.put(uri4, doc4);
		assertEquals(doc1, table.put(uri1, null));
		assertEquals(doc2, table.put(uri2, null));
		assertEquals(doc3, table.put(uri3, null));
		assertEquals(doc4, table.put(uri4, null));
		assertEquals(null, table.get(uri1));
		assertEquals(null, table.get(uri2));
		assertEquals(null, table.get(uri3));
		assertEquals(null, table.get(uri4));
	}
	
	@Test
	void putAndGetWithGenerics()
	{
		BTreeImpl<Integer, String> btree = new BTreeImpl<>();
		assertNull(btree.put(1, "one"));
		assertEquals("one", btree.put(1, "ONE"));
		assertNull(btree.put(22, "value"));
		assertEquals("value", btree.get(22));
	}
	
	@Test
	void moveToDiskWithoutPersistenceManager() throws Exception
	{
		BTreeImpl<Integer, String> btree = new BTreeImpl<>();
		btree.put(1, "one");
		try
		{
			btree.moveToDisk(1);
			fail();
		}
		catch (IllegalAccessError e)
		{
//			System.out.println(e);
		}
	}
	
	@Test
	void moveNonExistantKeyToDisk() throws Exception
	{
		table.put(uri1, doc1);
		try
		{
			table.moveToDisk(uri2);
			fail();
		}
		catch(IllegalArgumentException e)
		{
//			System.out.println(e);
		}
		
		table.put(uri1, null);
		
		try
		{
			table.moveToDisk(uri1);
			fail();
		}
		catch(IllegalArgumentException e)
		{
//			System.out.println(e);
		}
	}
	
	private static Document createDoc(URI uri, String txt)
	{
		return new DocumentImpl(uri, txt);
	}
	
	private static URI createUri(String input)
	{
		try
		{
			return new URI(input);
		}
		catch (URISyntaxException e)
		{
			e.printStackTrace();
		}
		return null;
	}
}
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import edu.yu.cs.com1320.project.impl.TrieImpl;

class TrieTest
{
	private static final Comparator<String> COMPARING = Comparator.comparing((String x) -> x);
	TrieImpl<String> trie= new TrieImpl<>();
	private final String str1= "he", str2= "hello", str3= "h", str4= "at", str5= "help", str6= "headache";
	
	@Test
	void put()
	{
		putValues();
		trie.put(str1, str6);
		
		List<String> list= trie.getAllWithPrefixSorted(str3, COMPARING);
		
//		printOutAll(list);
		
		assertEquals(str3, list.get(0));
		assertEquals(str1, list.get(1));
		assertEquals(str6, list.get(2));
		assertEquals(str2, list.get(3));
		assertEquals(str5, list.get(4));
		
//		printOutAll(trie.getAllSorted(str1, COMPARING));
	}
	
	@Test
	void getAllSorted()
	{
		ArrayList<String> set= new ArrayList<>();
		putValues();
		trie.put(str1, "abc");
		trie.put(str1, "will");
		trie.put(str5, "bat");
		
		set.add("abc");
		set.add(str1);
		set.add("will");
		
		assertEquals(set, trie.getAllSorted(str1, COMPARING));
		
		set.clear();
		set.add("bat");
		set.add(str5);
		
		assertEquals(set, trie.getAllSorted(str5, COMPARING));
	}
	
	@Test
	void getAllWithNoValues()
	{
		assertTrue(trie.getAllWithPrefixSorted(str6, COMPARING).isEmpty());
		assertTrue(trie.getAllSorted(str5, COMPARING).isEmpty());
		putValues();
		assertTrue(trie.getAllWithPrefixSorted("zz", COMPARING).isEmpty());
		assertTrue(trie.getAllSorted("hel", COMPARING).isEmpty());
	}
	
	@Test
	void deleteAll()
	{
		putValues();
		for(String val: trie.getAllSorted(str1,COMPARING))
		{
			System.out.println(val);
		}
		for(String val: trie.deleteAll(str1))
		{
			System.out.println(val);
		}
		
		for(String val: trie.getAllSorted(str1,COMPARING))
		{
			System.out.println(val);
		}
		
		trie.deleteAll(str1);
		assertTrue(trie.getAllSorted(str1, COMPARING).isEmpty());
		
		trie.deleteAll(str2);
		assertTrue(trie.getAllSorted(str2, COMPARING).isEmpty());
		
		trie.deleteAll(str3);
		assertTrue(trie.getAllSorted(str3, COMPARING).isEmpty());
		
		trie.deleteAll(str4);
		assertTrue(trie.getAllSorted(str4, COMPARING).isEmpty());
		
		trie.deleteAll(str5);
		assertTrue(trie.getAllSorted(str5, COMPARING).isEmpty());
		
		assertTrue(trie.getAllWithPrefixSorted("", COMPARING).isEmpty());
		
		assertTrue(trie.deleteAll(str1).isEmpty());
	}
	
	@SuppressWarnings({ "rawtypes" })
	@Test
	void deleteAllWithPrefix()
	{
		putValues();
		
		Set<String> set= new HashSet<>(trie.getAllWithPrefixSorted(str1, COMPARING));
		
		Set set2= trie.deleteAllWithPrefix(str1);
		
		assertEquals(set, set2);
		
		assertTrue(trie.getAllWithPrefixSorted(str1, COMPARING).isEmpty());
		
		List<String> notDeleted = trie.getAllWithPrefixSorted("", COMPARING);
		
		assertEquals(2, notDeleted.size());
		
		printOutAll(notDeleted);
		assertTrue(trie.getAllSorted(str1, COMPARING).isEmpty());
	}
	
	@Test
	void putWithNumbers()
	{
		TrieImpl<Integer> trie= new TrieImpl<>();
		trie.put("z123", 1);
		trie.put("09q", 2);
		trie.put("z123", 12);
		
		List<Integer> list1 = trie.getAllSorted("z123", new CompareInts());
		List<Integer> expected= new ArrayList<>();
		expected.add(1);
		expected.add(12);
		
		assertEquals(expected, list1);
		
		assertEquals(1, trie.getAllSorted("09q", new CompareInts()).size());
	}
	
	@Test
	void delete()
	{
		putValues();
		trie.put(str1, "when");
		trie.put(str1, "who");
		trie.put(str2, "you");
		
		assertEquals(str5, trie.delete(str5, str5));
		assertNull(trie.delete(str5, str5));
		assertNull(trie.delete("12", str1));
		
		assertEquals("when", trie.delete(str1, "when"));
		assertEquals("who", trie.delete(str1, "who"));
		assertEquals(str1, trie.delete(str1, str1));
		assertTrue(trie.getAllSorted(str1, COMPARING).isEmpty());
		
		assertEquals(str4, trie.delete(str4, str4));
		
		assertEquals(str2, trie.delete(str2, str2));
		
		HashSet<String> set = new HashSet<String>();
		set.add("you");
		assertEquals(set, new HashSet<String>(trie.getAllSorted(str2, COMPARING)));
		
//		printOutAll(trie.getAllWithPrefixSorted("", COMPARING));
		
		trie.delete(str3, str3);
		trie.delete(str2, "you");
	}
	
	private final class CompareInts implements Comparator<Integer> {
		@Override
		public int compare(Integer o1, Integer o2)
		{
			return o1-o2;
		}
	}

	private void putValues()
	{
		trie.put(str1, str1);
		trie.put(str2, str2);
		trie.put(str3, str3);
		trie.put(str4, str4);
		trie.put(str5, str5);
	}
	
	private static void printOutAll(Collection<String> collection)
	{
		for(String s: collection)
		{
			System.out.println(s);
		}
	}
}

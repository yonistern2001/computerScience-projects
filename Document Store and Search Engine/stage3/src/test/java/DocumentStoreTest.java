
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import edu.yu.cs.com1320.project.stage3.Document;
import edu.yu.cs.com1320.project.stage3.DocumentStore.DocumentFormat;
import edu.yu.cs.com1320.project.stage3.impl.DocumentImpl;
import edu.yu.cs.com1320.project.stage3.impl.DocumentStoreImpl;

public class DocumentStoreTest
{
	private final DocumentStoreImpl store= new DocumentStoreImpl();
	
	private static final String STRING1 ="DOC-1", STRING2 ="DOC-2";
	private static final byte[] BYTES1 = STRING1.getBytes();
	private static URI URI1 = createUri("uri-1"), URI2 = createUri("uri-2"), URI3 = createUri("uri-3");
	
	private static final String doc1= "look over here its very dark and gloomy today", doc2= "It's very nice over there and there to",
											doc3= "Hello there how are you doing today";
	
	@Test
	void addTextDocument() throws IOException
	{
		InputStream stream1= createDoc(STRING1);
		
		assertEquals(0,	store.putDocument(stream1, URI1, DocumentFormat.TXT));
		Document doc= store.getDocument(URI1);
		assertEquals(STRING1, doc.getDocumentTxt());
		assertNull(doc.getDocumentBinaryData());
		assertEquals(URI1, doc.getKey());
	}
	
	@Test
	void deleteDocument() throws IOException
	{
		InputStream stream1= createDoc(STRING1);
		
		store.putDocument(stream1, URI1, DocumentFormat.TXT);
		
		assertTrue(store.deleteDocument(URI1));
		assertEquals(null,store.getDocument(URI1));
	}
	
	@Test
	void replaceDocument() throws IOException
	{
		InputStream stream1= createDoc(STRING1);
		store.putDocument(stream1, URI1, DocumentFormat.TXT);
		Document doc= store.getDocument(URI1);
		
		String STRING2="bye";
		InputStream stream2= new ByteArrayInputStream(STRING2.getBytes());
		assertEquals(doc.hashCode(), store.putDocument(stream2, URI1, DocumentFormat.TXT));
		doc= store.getDocument(URI1);
		assertEquals(STRING2, doc.getDocumentTxt());
		assertEquals(null, doc.getDocumentBinaryData());
	}
	
	@Test
	void addNullDocument() throws IOException
	{
		InputStream stream1= createDoc(STRING1);
		store.putDocument(stream1, URI1, DocumentFormat.TXT);
		Document doc= store.getDocument(URI1);
		
		assertEquals(doc.hashCode(), store.putDocument(null, URI1, DocumentFormat.TXT));
		assertNull( store.getDocument(URI1));
	}
	
	@Test
	void addBinaryDocument() throws IOException
	{
		InputStream stream1= createDoc(STRING1);
		store.putDocument(stream1, URI1, DocumentFormat.BINARY);
		Document doc= store.getDocument(URI1);
		assertNull( doc.getDocumentTxt());
		assertEquals(URI1, doc.getKey());
		assertArrayEquals(BYTES1, doc.getDocumentBinaryData());
		assertTrue(doc.getWords().isEmpty());
	}
	
	@Test
	void addDocToUsedURI() throws IOException
	{
		final String txt1 = "a bam w";
		final String txt2 = "bat rag";
		
		store.putDocument(createDoc(txt1), URI1, DocumentFormat.TXT);
		assertEquals(txt1, store.getDocument(URI1).getDocumentTxt());
		assertEquals(1, store.search("bam").size());
		
		store.putDocument(createDoc(txt2), URI1, DocumentFormat.TXT);
		assertTrue(store.search("bam").isEmpty());
		assertEquals(txt2, store.getDocument(URI1).getDocumentTxt());
		
		store.undo();
		assertEquals(1, store.search("bam").size());
		assertEquals(txt1, store.getDocument(URI1).getDocumentTxt());
	}
	
	@Test
	void deleteDocsByPrefix() throws IOException
	{
		putDocuments();
		Set<URI> deleted = store.deleteAllWithPrefix("he");
		assertEquals(2, deleted.size());
		
		assertNull(store.getDocument(URI1));
		assertDocumentText(URI2, doc2);
		
		assertNull(store.getDocument(URI3));

		assertEquals(1, store.searchByPrefix("").size());
	}
	
	@Test
	void deleteAll() throws IOException
	{
		putDocuments();
		Set<URI> set = store.deleteAll("It's.?");
		HashSet<URI> expected = new HashSet<>();
		expected.add(URI1);
		expected.add(URI2);
		
		assertEquals(expected, set);
		
		assertNull(store.getDocument(URI1));
		assertNull(store.getDocument(URI2));
		assertDocumentText(URI3, doc3);
	}
	
	@Test
	void deleteAndUndo() throws IOException
	{
		InputStream stream1= createDoc(STRING1);
		assertEquals(0,	store.putDocument(stream1, URI1, DocumentFormat.TXT));
		Document doc= store.getDocument(URI1);
		assertDocumentText(URI1, STRING1);
		store.deleteDocument(URI1);
		assertNull(store.getDocument(URI1));
		store.undo(URI1);
		assertEquals(doc, store.getDocument(URI1));
	}
	
	@Test
	void undoNothingToUndo() throws IOException
	{
		try
		{
			store.undo();
			fail();
		}
		catch(IllegalStateException i)
		{
		}
	}
	
	@Test
	void undoAdd() throws IOException
	{
		store.putDocument(createDoc(STRING1), URI1, DocumentFormat.TXT);
		
		store.undo();
		assertNull(store.getDocument(URI1));
	}

	@Test
	void undoUpdate() throws IOException
	{
		store.putDocument(createDoc(STRING1), URI1, DocumentFormat.TXT);
		store.putDocument(createDoc(STRING2), URI1, DocumentFormat.TXT);
		
		store.undo();
		assertDocumentText(URI1, STRING1);
	}

	
	@Test
	void undoDelete() throws IOException
	{
		store.putDocument(createDoc(STRING1), URI1, DocumentFormat.TXT);
		store.deleteDocument(URI1);
		
		store.undo();
		assertDocumentText(URI1, STRING1);
	}
	
	@Test
	void undoByUri() throws IOException
	{
		store.putDocument(createDoc(STRING1), URI1, DocumentFormat.TXT);
		store.putDocument(createDoc(STRING2), URI2, DocumentFormat.TXT);
		
		store.undo(URI1);
		assertNull(store.getDocument(URI1));
		assertDocumentText(URI2, STRING2);

		store.undo(URI2);
		assertNull(store.getDocument(URI2));
	}
	
	@Test
	void undoByUriNothingToDo() throws IOException
	{
		store.putDocument(createDoc(STRING2), URI2, DocumentFormat.TXT);
		try
		{
			store.undo(URI1);
			fail();
		}
		catch(IllegalStateException i)
		{
		}
	}
	
	@Test
	void undoByUriWithDeleteAll() throws IOException
	{
		putDocuments();
		store.deleteAllWithPrefix("today");
		store.undo(URI1);
		assertDocumentText(URI1, doc1);
		assertDocumentText(URI2, doc2);
		assertNull(store.getDocument(URI3));
		
		store.undo();
		assertDocumentText(URI1, doc1);
		assertDocumentText(URI2, doc2);
		assertDocumentText(URI3, doc3);
	}
	
	@Test
	void getWordsInDoc() throws IOException
	{
		String message= "Today is Thursday and it's hot outside and. So we can do it";
		Document doc= new DocumentImpl(URI2, message);
		for(String word: doc.getWords())
		{
			System.out.print("word: "+word);
			System.out.println("  count: "+doc.wordCount(word));
		}
		
		assertEquals(2, doc.wordCount("and"));
		assertEquals(1, doc.wordCount("it"));
		assertEquals(0, doc.wordCount("andd"));
	}
	
	@Test
	void getWordCountWithBinaryDoc() throws IOException
	{
		InputStream stream1= createDoc(STRING1);
		store.putDocument(stream1, URI1, DocumentFormat.BINARY);
		Document doc= store.getDocument(URI1);
		assertEquals(0, doc.wordCount("d"));
	}
	
	@Test
	void search() throws IOException
	{
		putDocuments();
		
		ArrayList<Document> expectedDocs= new ArrayList<>();
		expectedDocs.add(store.getDocument(URI2));
		expectedDocs.add(store.getDocument(URI3));
		
		assertEquals(expectedDocs, store.search("there"));
		
		assertTrue(store.search("h").isEmpty());
	}
	
	@Test
	void getAllWithPrefix() throws IOException
	{
		putDocuments();
		
		List<Document> expectedDocs= new ArrayList<>();
		expectedDocs.add(store.getDocument(URI2));
		expectedDocs.add(store.getDocument(URI3));
		expectedDocs.add(store.getDocument(URI1));
		
		assertEquals(expectedDocs, store.searchByPrefix("T"));
		
		
		store.putDocument(createDoc("zn znd z"), URI1, DocumentFormat.TXT);
		store.putDocument(createDoc("zw z"), URI2, DocumentFormat.TXT);
		store.putDocument(createDoc("znswer"), URI3, DocumentFormat.TXT);
		
		expectedDocs.clear();
		
		expectedDocs.add(store.getDocument(URI1));
		expectedDocs.add(store.getDocument(URI2));
		expectedDocs.add(store.getDocument(URI3));
		
		assertEquals(expectedDocs, store.searchByPrefix("z"));
	}
	
	private void putDocuments() throws IOException
	{
		store.putDocument(createDoc(doc1), URI1, DocumentFormat.TXT);
		store.putDocument(createDoc(doc2), URI2, DocumentFormat.TXT);
		store.putDocument(createDoc(doc3), URI3, DocumentFormat.TXT);
	}
	
	private void assertDocumentText(URI uri, String text)
	{
		assertEquals(text, store.getDocument(uri).getDocumentTxt());
	}
	
	
	private static ByteArrayInputStream createDoc(String doc)
	{
		return new ByteArrayInputStream(doc.getBytes());
	}
	
	
	private static URI createUri(String uri) {
		try
		{
			return new URI(uri);
		}
		catch (URISyntaxException e)
		{
			throw  new RuntimeException(e);
		}
	}
}
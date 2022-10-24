
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

import org.junit.jupiter.api.Test;

import edu.yu.cs.com1320.project.stage2.Document;
import edu.yu.cs.com1320.project.stage2.DocumentStore.DocumentFormat;
import edu.yu.cs.com1320.project.stage2.impl.DocumentStoreImpl;

public class DocumentStoreTest
{
	private final DocumentStoreImpl store= new DocumentStoreImpl();

	private static final String STRING1 ="DOC-1", STRING2 ="DOC-2";
	private static final byte[] BYTES1 = STRING1.getBytes(),  BYTES2 = STRING2.getBytes();
	private static URI URI1 = createUri("uri-1"), URI2 = createUri("uri-2");
	
	
	
	@Test
	void addTextDocument() throws IOException
	{
		InputStream stream1= createDoc(STRING1);
		
		assertEquals(0,	store.putDocument(stream1, URI1, DocumentFormat.TXT));
		Document doc= store.getDocument(URI1);
		assertEquals(STRING1, doc.getDocumentTxt());
		assertEquals(null, doc.getDocumentBinaryData());
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
	}
	
	@Test
	void deleteAndUndo() throws IOException
	{
		InputStream stream1= createDoc(STRING1);
		assertEquals(0,	store.putDocument(stream1, URI1, DocumentFormat.TXT));
		Document doc= store.getDocument(URI1);
		assertEquals(STRING1, doc.getDocumentTxt());
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
		assertDocumnetText(URI1, STRING1);
	}

	
	@Test
	void undoDelete() throws IOException
	{
		store.putDocument(createDoc(STRING1), URI1, DocumentFormat.TXT);
		store.deleteDocument(URI1);
		
		store.undo();
		assertDocumnetText(URI1, STRING1);
	}
	
	@Test
	void undoByUri() throws IOException
	{
		store.putDocument(createDoc(STRING1), URI1, DocumentFormat.TXT);
		store.putDocument(createDoc(STRING2), URI2, DocumentFormat.TXT);
		
		store.undo(URI1);
		assertNull(store.getDocument(URI1));
		assertDocumnetText(URI2, STRING2);

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
	
	private void assertDocumnetText(URI uri, String text)
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
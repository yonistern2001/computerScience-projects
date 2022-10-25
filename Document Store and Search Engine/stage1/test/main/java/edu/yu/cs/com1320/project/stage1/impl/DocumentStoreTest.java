package edu.yu.cs.com1320.project.stage1.impl;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;

import edu.yu.cs.com1320.project.stage1.Document;
import edu.yu.cs.com1320.project.stage1.DocumentStore.DocumentFormat;

public class DocumentStoreTest
{
	private final DocumentStoreImpl store= new DocumentStoreImpl();

	private static final String STRING ="Hello";
	private static final byte[] BYTES = STRING.getBytes();
	private static URI uri1;
	static {
		 try {
			uri1=new URI("123");
		} catch (URISyntaxException e) {
		}
	}
	
	@Test
	void addTextDocument() throws IOException
	{
		InputStream stream1= new ByteArrayInputStream(BYTES);
		
		assertEquals(0,	store.putDocument(stream1, uri1, DocumentFormat.TXT));
		Document doc= store.getDocument(uri1);
		assertEquals(STRING, doc.getDocumentTxt());
		assertEquals(null, doc.getDocumentBinaryData());
		assertEquals(uri1, doc.getKey());
	}
	
	@Test
	void deleteDocument() throws IOException
	{
		InputStream stream1= new ByteArrayInputStream(BYTES);
		
		store.putDocument(stream1, uri1, DocumentFormat.TXT);
		
		assertTrue(store.deleteDocument(uri1));
		assertEquals(null,store.getDocument(uri1));
	}
	
	@Test
	void replaceDocument() throws IOException
	{
		InputStream stream1= new ByteArrayInputStream(BYTES);
		store.putDocument(stream1, uri1, DocumentFormat.TXT);
		Document doc= store.getDocument(uri1);
		
		String STRING2="bye";
		InputStream stream2= new ByteArrayInputStream(STRING2.getBytes());
		assertEquals(doc.hashCode(), store.putDocument(stream2, uri1, DocumentFormat.TXT));
		doc= store.getDocument(uri1);
		assertEquals(STRING2, doc.getDocumentTxt());
		assertEquals(null, doc.getDocumentBinaryData());
	}
	
	@Test
	void addNullDocument() throws IOException
	{
		InputStream stream1= new ByteArrayInputStream(BYTES);
		store.putDocument(stream1, uri1, DocumentFormat.TXT);
		Document doc= store.getDocument(uri1);
		
		assertEquals(doc.hashCode(), store.putDocument(null, uri1, DocumentFormat.TXT));
		assertNull( store.getDocument(uri1));
	}
	
	@Test
	void addBinaryDocument() throws IOException
	{
		InputStream stream1= new ByteArrayInputStream(BYTES);
		store.putDocument(stream1, uri1, DocumentFormat.BINARY);
		Document doc= store.getDocument(uri1);
		assertNull( doc.getDocumentTxt());
		assertEquals(uri1, doc.getKey());
		assertArrayEquals(BYTES, doc.getDocumentBinaryData());
	}
}
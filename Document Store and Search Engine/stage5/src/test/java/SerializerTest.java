import static edu.yu.cs.com1320.project.stage5.impl.DocumentPersistenceManager.GSON;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;

import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.impl.DocumentImpl;

class SerializerTest
{
	@Test
	void serializeDoc() throws URISyntaxException
	{
		Document doc = new DocumentImpl(new URI("abc"), "word");
		Document result= serialize(doc);
		assertDocsEqual(doc, result);
	}
	
	@Test
	void serializeBytesDoc() throws URISyntaxException
	{
		String words= "Right now?";
		byte[] byteArray= words.getBytes();
		Document doc = new DocumentImpl(new URI("abc"), byteArray);
		Document result= serialize(doc);
		assertDocsEqual(doc, result);
	}

	private void assertDocsEqual(Document doc1, Document doc2)
	{
		assertEquals(doc1, doc2);
		assertEquals(doc1.getDocumentTxt(), doc2.getDocumentTxt());
		assertEquals(doc1.getKey(), doc2.getKey());
		assertArrayEquals(doc1.getDocumentBinaryData(), doc2.getDocumentBinaryData());
		assertEquals(doc1.getWordMap(), doc2.getWordMap());
	}

	private static Document serialize(Document doc)
	{
		String json= GSON.toJson(doc);
		System.out.println(json);
		return GSON.fromJson(json, DocumentImpl.class);
	}
}
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.impl.DocumentImpl;
import edu.yu.cs.com1320.project.stage5.impl.DocumentPersistenceManager;

public class PersistenceManagerTest
{	
	private static final String HTTP_WWW_GOOGLE_COM_DOCUMENTS = "http://www.google.com/documents";
	
	@TempDir
	public File baseDirectory;
	
	@Test
	void serializeDeserializeDeleteTest() throws URISyntaxException, IOException
	{
		DocumentPersistenceManager manager = new DocumentPersistenceManager(null);
		File file= new File(System.getProperty("user.dir")+"/www.google.com/documents.json");
		URI uri= new URI(HTTP_WWW_GOOGLE_COM_DOCUMENTS);
		DocumentImpl document = new DocumentImpl(uri, "texts as");
		manager.serialize(uri, document);
		assertTrue(file.exists());
		Document deserializeDoc= manager.deserialize(uri);
		assertEquals(document, deserializeDoc);
		manager.delete(uri);
		assertTrue(!file.exists());
	}
	
	@Test
	void deleteNonExistantDoc() throws URISyntaxException, IOException
	{
		DocumentPersistenceManager manager = new DocumentPersistenceManager(null);
		URI uri= new URI(HTTP_WWW_GOOGLE_COM_DOCUMENTS);
		assertFalse(manager.delete(uri));
	}
	
	@Test
	void serializeDeserializeDeleteTestWithBaseDir() throws URISyntaxException, IOException
	{
		String baseDir= "/C:/Users/Yoni Stern/Downloads/";
		DocumentPersistenceManager manager = new DocumentPersistenceManager(new File(baseDir));
		File file= new File(baseDir+"/www.google.com/documents.json");
		URI uri= new URI(HTTP_WWW_GOOGLE_COM_DOCUMENTS);
		DocumentImpl document = new DocumentImpl(uri, "texts as");
		manager.serialize(uri, document);
		assertTrue(file.exists());
		Document deserializeDoc= manager.deserialize(uri);
		assertEquals(document, deserializeDoc);
		manager.delete(uri);
		assertTrue(!file.exists());
	}

}
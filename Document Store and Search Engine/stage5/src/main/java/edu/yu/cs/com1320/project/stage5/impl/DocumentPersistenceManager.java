package edu.yu.cs.com1320.project.stage5.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.PersistenceManager;
import jakarta.xml.bind.DatatypeConverter;

/**
 * created by the document store and given to the BTree via a call to BTree.setPersistenceManager
 */
public class DocumentPersistenceManager implements PersistenceManager<URI, Document>
{
	public static final Gson GSON = new GsonBuilder().registerTypeAdapter(DocumentImpl.class, new DocumentDeserializer()).
			registerTypeAdapter(DocumentImpl.class, new DocumentSerializer()).create();
	private static final String TEXT = "text", BYTE_DATA = "byteData", URI= "uri", WORD_MAP= "wordMap";
	
	private final File baseDir;
	private final String baseDirPath;
	
    public DocumentPersistenceManager(File baseDir)
    {
    	if(baseDir == null)
    	{
    		this.baseDirPath= System.getProperty("user.dir");
    		this.baseDir= new File(baseDirPath);
    	}
    	else
    	{
    		this.baseDir= baseDir;
    		this.baseDirPath= baseDir.getPath();
    	}
    }

    @Override
    public void serialize(URI uri, Document val) throws IOException
    {
    	File file= createFileAndDirectorys(createPath(uri));
    	file.createNewFile();
    	String output= GSON.toJson(val);
    	FileOutputStream outputStream= new FileOutputStream(file);
    	outputStream.write(output.getBytes());
    	outputStream.close();
    }
    
    @Override
    public Document deserialize(URI uri) throws IOException
    {
    	FileInputStream inputStream= new FileInputStream(createPath(uri));
    	String fileOutput= new String(inputStream.readAllBytes());
    	inputStream.close();
    	return GSON.fromJson(fileOutput, DocumentImpl.class);
    }

    @Override
    public boolean delete(URI uri) throws IOException
    {
    	File file= new File(createPath(uri));
    	boolean output= file.delete();
    	File curr= file.getParentFile();
    	if(output != false)
    	{
    		while(!(baseDir.equals(curr)))
    		{
    			if(!curr.delete())
    			{
    				return output;
    			}
    			curr= curr.getParentFile();
    		}
    	}
    	return output;
    }
        
    private String createPath(URI uri)
    {
    	String path= uri.getRawSchemeSpecificPart();
    	return baseDirPath+path+".json";
    }
    
    private static File createFileAndDirectorys(String path) throws IOException
    {
    	File file= new File(path);
    	File dirs= file.getParentFile();
    	dirs.mkdirs();
    	file.createNewFile();
    	return file;
    }
    
	private static class DocumentSerializer implements JsonSerializer<Document>
	{
		@Override
		public JsonElement serialize(Document src, Type typeOfSrc, JsonSerializationContext context)
		{
			JsonObject json= new JsonObject();
			String txt= src.getDocumentTxt();
			if(txt != null)
			{
				json.addProperty(TEXT, txt);
			}
			else
			{
				json.addProperty(BYTE_DATA, DatatypeConverter.printBase64Binary(src.getDocumentBinaryData()));
			}
			json.add(URI, context.serialize(src.getKey()));
			json.add(WORD_MAP, context.serialize(src.getWordMap()));
			return json;
		}
	}
	
	private static class DocumentDeserializer implements JsonDeserializer<Document>
	{
		private static final Type MAP_TYPE = new TypeToken<Map<String, Integer>>(){}.getType();
		
		@Override
		public Document deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException
		{
			JsonObject jsonObject= json.getAsJsonObject();
			JsonElement textObject= jsonObject.getAsJsonPrimitive(TEXT);
			URI uri= context.deserialize(jsonObject.get(URI), URI.class);
			Document doc;
			if(textObject != null)
			{
				Map <String, Integer> wordMap= context.deserialize(jsonObject.get(WORD_MAP), MAP_TYPE);
				doc= new DocumentImpl(uri, textObject.getAsString(), wordMap);
			}
			else
			{
				String bytes= jsonObject.getAsJsonPrimitive(BYTE_DATA).getAsString();
				byte[] byteArray = DatatypeConverter.parseBase64Binary(bytes);
				doc= new DocumentImpl(uri, byteArray);
			}
			return doc;
		}
	}
}
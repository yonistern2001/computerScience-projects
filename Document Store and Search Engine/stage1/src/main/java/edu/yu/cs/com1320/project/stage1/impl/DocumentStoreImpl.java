package edu.yu.cs.com1320.project.stage1.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import edu.yu.cs.com1320.project.HashTable;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.stage1.Document;
import edu.yu.cs.com1320.project.stage1.DocumentStore;

public class DocumentStoreImpl implements DocumentStore
{
	private final HashTable<URI, Document> store= new HashTableImpl<>();
	
	@Override
	public int putDocument(InputStream input, URI uri, DocumentFormat format) throws IOException
	{
		if(uri==null)
		{
			throw new IllegalArgumentException();
		}
		int output= 0;
		Document oldDoc= getDocument(uri);
		if(oldDoc!=null)
		{
			output= oldDoc.hashCode();
		}
		if(input == null)
		{
			deleteDocument(uri);
			return output;
		}
		
		if(format==null)
		{
			throw new IllegalArgumentException();
		}
		
		DocumentImpl doc = createDocument(input, uri, format);
		store.put(uri, doc);
		return output;
	}

	private static DocumentImpl createDocument(InputStream input, URI uri, DocumentFormat format) throws IOException
	{
		byte[] bytes = input.readAllBytes();
		if(format.equals(DocumentFormat.TXT))
		{
			return new DocumentImpl(uri, new String(bytes));
		}
		return new DocumentImpl(uri, bytes);	
	}

	@Override
	public Document getDocument(URI uri)
	{
		return store.get(uri);
	}

	@Override
	public boolean deleteDocument(URI uri)
	{
		return store.put(uri, null) != null;
	}

}

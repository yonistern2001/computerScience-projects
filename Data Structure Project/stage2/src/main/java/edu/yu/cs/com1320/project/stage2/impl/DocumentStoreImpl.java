package edu.yu.cs.com1320.project.stage2.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import edu.yu.cs.com1320.project.Command;
import edu.yu.cs.com1320.project.HashTable;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.stage2.Document;
import edu.yu.cs.com1320.project.stage2.DocumentStore;

public class DocumentStoreImpl implements DocumentStore
{
	private final HashTable<URI, Document> store= new HashTableImpl<>();
	private final StackImpl<Command> stack= new StackImpl<>();
	
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
		addUndo(uri, oldDoc);
		return output;
	}

	private void addUndo(URI uri, Document oldDoc) {
		stack.push(new Command(uri, u ->{
			store.put(u, oldDoc);
			return Boolean.TRUE;}));
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
		Document deleted = store.put(uri, null);
		addUndo(uri, deleted);
		return deleted != null;
	}

	@Override
	public void undo() throws IllegalStateException
	{
		if(stack.peek() == null)
		{
			throw new IllegalStateException();
		}
		stack.pop().undo();
	}

	@Override
	public void undo(URI uri) throws IllegalStateException
	{
		Command command= stack.pop();
		if(command == null)
		{
			throw new IllegalStateException("URI not used");
		}
		if(command.getUri().equals(uri))
		{
			command.undo();
			return;
		}
		undo(uri);
		stack.push(command);
	}
}

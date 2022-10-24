package edu.yu.cs.com1320.project.stage5.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.yu.cs.com1320.project.BTree;
import edu.yu.cs.com1320.project.CommandSet;
import edu.yu.cs.com1320.project.GenericCommand;
import edu.yu.cs.com1320.project.Undoable;
import edu.yu.cs.com1320.project.impl.BTreeImpl;
import edu.yu.cs.com1320.project.impl.MinHeapImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.DocumentStore;

public class DocumentStoreImpl implements DocumentStore
{
	private final BTree<URI, Document> store= new BTreeImpl<>();
	private final StackImpl<Undoable> undoStack= new StackImpl<>();
	private final TrieImpl<URI> docsByWords= new TrieImpl<>();
	private final MinHeapImpl<URI> lruUris= new MinHeapImpl<>(new DocumentComparator());
	private int maxBytes= -1, maxCount= -1, bytesCount= 0, docCount= 0;
	private final Set<URI> onDisk= new HashSet<>();
	
	public DocumentStoreImpl(File baseDir)
	{
		DocumentPersistenceManager manager = new DocumentPersistenceManager(baseDir);
		store.setPersistenceManager(manager);
	}
	
	public DocumentStoreImpl()
	{
		this(null);
	}
	
	@Override
	public int putDocument(InputStream input, URI uri, DocumentFormat format) throws IOException
	{
		if(uri==null || format == null)
		{
			throw new IllegalArgumentException();
		}
		Document doc, oldDoc;
		doc= input == null ? null : createDocument(input, uri, format);	
		oldDoc= addDocument(uri, doc);
		undoStack.push(createUndoFunction(uri, oldDoc));
		if(oldDoc != null)
		{
			return oldDoc.hashCode();
		}
		return 0;
	}

	private Document addDocument(URI uri, Document doc)
	{
		Document oldDoc= doDeleteDocument(uri);
		if(doc == null)
		{
			return oldDoc;
		}		
		if(maxBytes != -1 && getDocSize(doc) > maxBytes)
		{
			throw new IllegalArgumentException("the doc being added is is bigger than the max byte limit");
		}
		store.put(uri, doc);
		addToHeap(uri, doc);
		Set<String> words = doc.getWords();
		addToTrie(uri, words);
		return oldDoc;
	}
	
	private void addToHeap(URI uri, Document doc)
	{
		bytesCount+= getDocSize(doc);
		docCount++;
		limitStorage();
		lruUris.insert(uri);
		setViewed(doc);
	}

	private void limitStorage()
	{
		if(maxBytes != -1)
		{
			while(bytesCount > maxBytes)
			{
				moveLruToDisk();
			}
		}
		if(maxCount != -1)
		{
			while(docCount > maxCount)
			{
				moveLruToDisk();
			}
		}
	}
	
	private void moveLruToDisk()
	{
		URI uri= lruUris.remove();
		int numBytes= getDocSize(doGetDocument(uri));
		try
		{
			store.moveToDisk(uri);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		docCount--;
		bytesCount-= numBytes;
		onDisk.add(uri);
	}
	
	private GenericCommand<URI> createUndoFunction(URI uri, Document doc)
	{
		return new GenericCommand<URI>(uri, u ->{
			addDocument(uri, doc);
			return Boolean.TRUE;
		});
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
		Document doc = doGetDocument(uri);
		setViewed(doc);
		return doc;
	}

	private Document doGetDocument(URI uri)
	{
		Document doc= store.get(uri);
		if(onDisk.remove(uri))
		{
			addToHeap(uri, doc);
		}
		return doc;
	}

	@Override
	public boolean deleteDocument(URI uri)
	{
		Document deleted = doDeleteDocument(uri);
		undoStack.push(createUndoFunction(uri, deleted));
		return deleted != null;
	}

	private Document doDeleteDocument(URI uri)
	{
		Document deleted = doGetDocument(uri);
		if(deleted != null)
		{
			Set<String> oldWords= deleted.getWords();
			deleteFromTrie(uri, oldWords);
			docCount--;
			bytesCount-= getDocSize(deleted);
			deleteFromHeap(uri);
		}
		return store.put(uri, null);
	}

	@Override
	public void undo() throws IllegalStateException
	{
		if(undoStack.peek() == null)
		{
			throw new IllegalStateException("Empty stack");
		}
		undoStack.pop().undo();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void undo(URI uri) throws IllegalStateException
	{
		Undoable command= undoStack.peek();
		if(command == null)
		{
			throw new IllegalStateException("URI not used");
		}
		
		if(command instanceof CommandSet)
		{
			CommandSet<URI> commandSet= (CommandSet<URI>) command;
			if(commandSet.undo(uri))
			{
				if(commandSet.isEmpty())
				{
					undoStack.pop();
				}
				return;
			}
		}
		else
		{
			GenericCommand<URI> com = (GenericCommand<URI>) command;
			if(com.getTarget().equals(uri))
			{
				command.undo();
				undoStack.pop();
				return;
			}
		}
		undoStack.pop();
		undo(uri);
		undoStack.push(command);
	}

	@Override
	public List<Document> search(String keyword)
	{
		keyword= createWord(keyword);
		List<URI> uris= docsByWords.getAllSorted(keyword, new Compare<Document>(keyword));
		List<Document> docs= getDocs(uris);
		setViewed(docs);
		return docs;
	}

	@Override
	public List<Document> searchByPrefix(String keywordPrefix)
	{
		keywordPrefix= createWord(keywordPrefix);
		List<URI> uris= docsByWords.getAllWithPrefixSorted(keywordPrefix, new CompareWithPrefix<Document>(keywordPrefix));
		List<Document> docs= getDocs(uris);
		setViewed(docs);
		return docs;
	}
	
	@Override
	public Set<URI> deleteAll(String keyword)
	{
		keyword= createWord(keyword);
		Set<URI> deleted= docsByWords.deleteAll(keyword);
		deleteDocs(deleted);
		return deleted;
	}

	@Override
	public Set<URI> deleteAllWithPrefix(String keywordPrefix)
	{
		keywordPrefix= createWord(keywordPrefix);
		Set<URI> deleted= docsByWords.deleteAllWithPrefix(keywordPrefix);
		return deleteDocs(deleted);
	}
	
	@Override
	public void setMaxDocumentCount(int limit)
	{
		if(limit < 0)
		{
			throw new IllegalArgumentException();
		}
		maxCount= limit;
		limitStorage();
	}

	@Override
	public void setMaxDocumentBytes(int limit)
	{
		if(limit < 0)
		{
			throw new IllegalArgumentException();
		}
		maxBytes= limit;
		limitStorage();
	}
	
	private void setViewed(Document doc)
	{
		if(doc == null)
		{
			return;
		}
		doc.setLastUseTime(System.nanoTime());
		lruUris.reHeapify(doc.getKey());
	}
	
	private void setViewed(Collection<Document> docs)
	{
		for(Document doc: docs)
		{
			setViewed(doc);
		}
	}
	
	private static int getDocSize(Document doc)
	{
		byte[] bytes;
		if(doc.getDocumentTxt() != null)
		{
			bytes= doc.getDocumentTxt().getBytes();
		}
		else
		{
			bytes= doc.getDocumentBinaryData();
		}
		return bytes.length;
	}
	
	private void deleteFromHeap(URI uri)
	{
		URI document= lruUris.remove();
		if(uri.equals(document))
		{
			return;
		}
		deleteFromHeap(uri);
		lruUris.insert(document);
	}

	private Set<URI> deleteDocs(Set<URI> uris)
	{
		CommandSet<URI> commandSet= new CommandSet<>();
		for(URI uri: uris)
		{
			Document deleted= doDeleteDocument(uri);
			commandSet.addCommand(createUndoFunction(uri, deleted));
			uris.add(uri);
		}
		undoStack.push(commandSet);
		return uris;
	}

	private static String createWord(String keyword)
	{
		String word= "";
		for(int i=0; i<keyword.length();i++)
		{
			char letter= keyword.charAt(i);
			if(Character.isLetterOrDigit(letter))
			{
				word+=letter;
			}
		}
		return word.toLowerCase();
	}
	
	private void addToTrie(URI doc, Set<String> words)
	{
		for(String word: words)
		{
			docsByWords.put(word, doc);
		}
	}
	
	private void deleteFromTrie(URI doc, Set<String> words)
	{
		for(String word: words)
		{
			docsByWords.delete(word, doc);
		}
	}
	
	private List<Document> getDocs(List<URI> uris)
	{
		List<Document> docs= new ArrayList<>();
		for(URI uri: uris)
		{
			docs.add(doGetDocument(uri));
		}
		return docs;
	}
	
	private class DocumentComparator implements Comparator<URI>
	{
		@Override
		public int compare(URI o1, URI o2)
		{
			if(onDisk.contains(o1) || onDisk.contains(o2))
			{
				throw new IllegalAccessError();
			}
			return store.get(o1).compareTo(store.get(o2));
		}
	}
	
	private class Compare<T> implements Comparator<URI>
	{
		private final String word;
		
		public Compare(String word)
		{
			this.word= word;
		}
		
		@Override
		public int compare(URI o1, URI o2)
		{
			Document doc_1 = doGetDocument(o1), doc_2= doGetDocument(o2);
			int doc1= doc_1.wordCount(word);
			int doc2= doc_2.wordCount(word);
			return doc2-doc1;
		}
	}
	
	private class CompareWithPrefix<T> implements Comparator<URI>
	{
		private final String prefix;
		
		public CompareWithPrefix(String prefix)
		{
			this.prefix= prefix;
		}
		
		@Override
		public int compare(URI o1, URI o2)
		{
			Document doc_1 = doGetDocument(o1), doc_2= doGetDocument(o2);
			int doc1= getNumOfWords(doc_1, getWordsWithPrefix(doc_1.getWords()));
			int doc2= getNumOfWords(doc_2, getWordsWithPrefix(doc_2.getWords()));
			return doc2-doc1;
		}

		private int getNumOfWords(Document doc, Set<String> words)
		{
			int numOfWords= 0;
			for(String word: words)
			{
				numOfWords+= doc.wordCount(word);
			}
			return numOfWords;
		}

		private Set<String> getWordsWithPrefix(Set<String> words)
		{
			Set<String> hasPrefix= new HashSet<>();
			for(String word: words)
			{
				if(startsWithPrefix(word))
				{
					hasPrefix.add(word);
				}
			}
			return hasPrefix;
		}
		
		private Boolean startsWithPrefix(String word)
		{
			for(int i=0; i<prefix.length(); i++)
			{
				char letter1= prefix.charAt(i);
				char letter2= word.charAt(i);
				if(letter1 != letter2)
				{
					return false;
				}
			}
			return true;
		}
	}
}
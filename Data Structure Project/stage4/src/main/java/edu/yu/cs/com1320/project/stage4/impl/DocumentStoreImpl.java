package edu.yu.cs.com1320.project.stage4.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.yu.cs.com1320.project.CommandSet;
import edu.yu.cs.com1320.project.GenericCommand;
import edu.yu.cs.com1320.project.HashTable;
import edu.yu.cs.com1320.project.Undoable;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.impl.MinHeapImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.stage4.Document;
import edu.yu.cs.com1320.project.stage4.DocumentStore;

public class DocumentStoreImpl implements DocumentStore
{
	private final HashTable<URI, Document> store= new HashTableImpl<>();
	private final StackImpl<Undoable> stack= new StackImpl<>();
	private final TrieImpl<Document> trie= new TrieImpl<>();
	private final MinHeapImpl<Document> heap= new MinHeapImpl<>();
	private int maxBytes= -1, maxCount= -1, bytesCount= 0, docCount= 0;
	
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
		stack.push(createUndoFunction(uri, oldDoc));
		if(oldDoc != null)
		{
			return oldDoc.hashCode();
		}
		return 0;
	}

	private Document addDocument(URI uri, Document doc)
	{
		Document oldDoc= deleteDoc(uri);
		if(doc == null)
		{
			return oldDoc;
		}		
		if(maxBytes != -1 && getDocSize(doc) > maxBytes)
		{
			throw new IllegalArgumentException("the doc being added is is bigger than the max byte limit");
		}
		store.put(uri, doc);
		addToHeap(doc);
		Set<String> words = doc.getWords();
		addToTrie(doc, words);
		return oldDoc;
	}
	
	private void addToHeap(Document doc)
	{
		bytesCount+= getDocSize(doc);
		docCount++;
		limitStorage();
		heap.insert(doc);
		setViewed(doc);
	}

	private void limitStorage()
	{
		if(maxBytes != -1)
		{
			while(bytesCount > maxBytes)
			{
				removeMinFromHeap();
			}
		}
		if(maxCount != -1)
		{
			while(docCount > maxCount)
			{
				removeMinFromHeap();
			}
		}
	}
	
	private void removeMinFromHeap()
	{
		Document doc= heap.remove();
		bytesCount-= getDocSize(doc);
		docCount--;
		URI uri = doc.getKey();
		store.put(uri, null);
		deleteFromTrie(doc, doc.getWords());
		removeDocFromStack(uri);
	}

	@SuppressWarnings("unchecked")
	private void removeDocFromStack(URI docUri)
	{
		if(stack.size() == 0)
		{
			return;
		}
		Undoable current= stack.pop();
		if(current instanceof GenericCommand)
		{
			GenericCommand<URI> command = (GenericCommand<URI>) current;
			if(command.getTarget().equals(docUri))
			{
				removeDocFromStack(docUri);
				return;
			}
		}
		else
		{
			CommandSet<URI> commandSet = (CommandSet<URI>) current;
			if(commandSet.containsTarget(docUri))
			{
				CommandSet<URI> temp= removeDocFromCommandSet(docUri, commandSet);
				if(temp.isEmpty())
				{
					removeDocFromStack(docUri);
					return;
				}
				current= temp;
			}
		}
		removeDocFromStack(docUri);
		stack.push(current);
	}

	private static CommandSet<URI> removeDocFromCommandSet(URI docUri, CommandSet<URI> commandSet)
	{
		CommandSet<URI> output = new CommandSet<>();
		for(GenericCommand<URI> command: commandSet)
		{
			if(!command.getTarget().equals(docUri))
			{
				output.addCommand(command);
			}
		}
		return output;
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
		Document doc= store.get(uri);
		setViewed(doc);
		return doc;
	}

	@Override
	public boolean deleteDocument(URI uri)
	{
		Document deleted = deleteDoc(uri);
		stack.push(createUndoFunction(uri, deleted));
		return deleted != null;
	}

	private Document deleteDoc(URI uri)
	{
		Document deleted = store.put(uri, null);
		if(deleted != null)
		{
			Set<String> oldWords= deleted.getWords();
			deleteFromTrie(deleted, oldWords);
			deleteFromHeap(deleted);
		}
		return deleted;
	}

	@Override
	public void undo() throws IllegalStateException
	{
		if(stack.peek() == null)
		{
			throw new IllegalStateException("Empty stack");
		}
		stack.pop().undo();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void undo(URI uri) throws IllegalStateException
	{
		Undoable command= stack.peek();
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
					stack.pop();
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
				stack.pop();
				return;
			}
		}
		stack.pop();
		undo(uri);
		stack.push(command);
	}

	@Override
	public List<Document> search(String keyword)
	{
		keyword= createWord(keyword);
		List<Document> docs= trie.getAllSorted(keyword, new Compare<Document>(keyword));
		setViewed(docs);
		return docs;
	}

	@Override
	public List<Document> searchByPrefix(String keywordPrefix)
	{
		keywordPrefix= createWord(keywordPrefix);
		List<Document> docs= trie.getAllWithPrefixSorted(keywordPrefix, new CompareWithPrefix<Document>(keywordPrefix));
		setViewed(docs);
		return docs;
	}

	@Override
	public Set<URI> deleteAll(String keyword)
	{
		keyword= createWord(keyword);
		Set<Document> deleted= trie.deleteAll(keyword);
		return deleteDocs(deleted);
	}

	@Override
	public Set<URI> deleteAllWithPrefix(String keywordPrefix)
	{
		keywordPrefix= createWord(keywordPrefix);
		Set<Document> deleted= trie.deleteAllWithPrefix(keywordPrefix);
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
		heap.reHeapify(doc);
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
	
	private void deleteFromHeap(Document doc)
	{
		Document document= heap.remove();
		if(doc.equals(document))
		{
			docCount--;
			bytesCount-= getDocSize(doc);
			return;
		}
		deleteFromHeap(doc);
		heap.insert(document);
	}
	
	private Set<URI> deleteDocs(Set<Document> docs)
	{
		CommandSet<URI> commandSet= new CommandSet<>();
		HashSet<URI> uris= new HashSet<>();
		for(Document doc: docs)
		{
			URI uri= doc.getKey();
			deleteDoc(uri);
			commandSet.addCommand(createUndoFunction(uri, doc));
			uris.add(uri);
		}
		stack.push(commandSet);
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
	
	private void addToTrie(Document doc, Set<String> words)
	{
		for(String word: words)
		{
			trie.put(word, doc);
		}
	}
	
	private void deleteFromTrie(Document doc, Set<String> words)
	{
		for(String word: words)
		{
			trie.delete(word, doc);
		}
	}
	
	private class Compare<T> implements Comparator<Document>
	{
		private final String word;
		
		public Compare(String word)
		{
			this.word= word;
		}
		
		@Override
		public int compare(Document o1, Document o2)
		{
			int doc1= o1.wordCount(word);
			int doc2= o2.wordCount(word);
			return doc2-doc1;
		}
	}
	
	private class CompareWithPrefix<T> implements Comparator<Document>
	{
		private final String prefix;
		
		public CompareWithPrefix(String prefix)
		{
			this.prefix= prefix;
		}
		
		@Override
		public int compare(Document o1, Document o2)
		{
			int doc1= getNumOfWords(o1, getWordsWithPrefix(o1.getWords()));
			int doc2= getNumOfWords(o2, getWordsWithPrefix(o2.getWords()));
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
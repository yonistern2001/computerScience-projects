package edu.yu.cs.com1320.project.stage3.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.yu.cs.com1320.project.CommandSet;
import edu.yu.cs.com1320.project.GenericCommand;
import edu.yu.cs.com1320.project.HashTable;
import edu.yu.cs.com1320.project.Undoable;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.stage3.Document;
import edu.yu.cs.com1320.project.stage3.DocumentStore;

public class DocumentStoreImpl implements DocumentStore
{
	private final HashTable<URI, Document> store= new HashTableImpl<>();
	private final StackImpl<Undoable> stack= new StackImpl<>();
	private final TrieImpl<Document> trie= new TrieImpl<>();
	
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
		Set<String> oldWords = getOldWords(oldDoc);
		DocumentImpl doc= createDocument(input, uri, format);
		store.put(uri, doc);
		Set<String> words = doc.getWords();
		deleteFromTrie(oldDoc, oldWords);
		addToTrie(doc, words);
		
		stack.push(createUndoFunction(uri, oldDoc, oldWords, doc, words));
		return output;
	}

	private Set<String> getOldWords(Document oldDoc)
	{
		if(oldDoc == null)
		{
			return new HashSet<>();
		}
		return oldDoc.getWords();
	}

	private GenericCommand<URI> createUndoFunctionForDelete(URI uri, Document oldDoc, Set<String> oldWords)
	{
		return createUndoFunction(uri, oldDoc, oldWords, null, new HashSet<String>());
	}
	
	private GenericCommand<URI> createUndoFunction(URI uri, Document docToAdd, Set<String> wordsToAdd, Document docToDelete, Set<String> wordsToDelete)
	{
		return new GenericCommand<URI>(uri, u ->{
			store.put(u, docToAdd);
			addToTrie(docToAdd, wordsToAdd);
			deleteFromTrie(docToDelete, wordsToDelete);
			return Boolean.TRUE;});
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
		Document deleted= getDocument(uri);
		stack.push(deleteDoc(uri));
		return deleted != null;
	}

	private GenericCommand<URI> deleteDoc(URI uri)
	{
		Document deleted = store.put(uri, null);
		Set<String> oldWords= getOldWords(deleted);
		deleteFromTrie(deleted, oldWords);
		return createUndoFunctionForDelete(uri, deleted, oldWords);
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
		return trie.getAllSorted(keyword, new Compare<Document>(keyword));
	}

	@Override
	public List<Document> searchByPrefix(String keywordPrefix)
	{
		keywordPrefix= createWord(keywordPrefix);
		return trie.getAllWithPrefixSorted(keywordPrefix, new CompareWithPrefix<Document>(keywordPrefix));
	}

	@Override
	public Set<URI> deleteAll(String keyword)
	{
		keyword= createWord(keyword);
		Set<Document> deleted = trie.deleteAll(keyword);
		return deleteDocs(deleted);
	}

	@Override
	public Set<URI> deleteAllWithPrefix(String keywordPrefix)
	{
		keywordPrefix= createWord(keywordPrefix);
		Set<Document> deleted= trie.deleteAllWithPrefix(keywordPrefix);
		return deleteDocs(deleted);
	}
	
	private Set<URI> deleteDocs(Set<Document> docs)
	{
		CommandSet<URI> commandSet= new CommandSet<>();
		HashSet<URI> uris= new HashSet<>();
		for(Document doc: docs)
		{
			URI uri= doc.getKey();
			commandSet.addCommand(deleteDoc(uri));
			uris.add(uri);
		}
		stack.push(commandSet);
		return uris;
	}

	private static String createWord(String keyword)
	{
		String word="";
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
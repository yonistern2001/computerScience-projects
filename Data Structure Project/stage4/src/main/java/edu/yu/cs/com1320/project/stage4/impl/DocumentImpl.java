package edu.yu.cs.com1320.project.stage4.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.yu.cs.com1320.project.stage4.Document;

public class DocumentImpl implements Document
{
	private URI uri;
	private String text;
	private byte[] binaryData;
	private final HashMap<String, Integer> map= new HashMap<>();
	private long lastUsed;
	
	public DocumentImpl(URI uri,String txt)
	{
		this.uri=uri;
		this.text=txt;
		mapOutTxt();
	}

	public DocumentImpl(URI uri, byte[] binaryData)
	{
		this.uri= uri;
		this.binaryData=binaryData;
	}
	
	@Override
	public String getDocumentTxt()
	{
		return text;
	}

	@Override
	public byte[] getDocumentBinaryData()
	{
		return binaryData;
	}

	@Override
	public URI getKey()
	{
		return uri;
	}
	
	@Override
	public int hashCode()
	{
		int result= uri.hashCode();
		result=31*result+(text != null?text.hashCode():0);
		result=31*result+Arrays.hashCode(binaryData);
		return result;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if(!(o instanceof Document))
		{
			return false;
		}
		return o.hashCode() == this.hashCode();
	}

	@Override
	public int wordCount(String word)
	{
		List<String> words= createWords(word);
		if(words.size() != 1)
		{
			throw new IllegalArgumentException("\""+word+"\" is not one word");
		}
		word= words.get(0).toLowerCase();
		if(map.get(word) == null)
		{
			return 0;
		}
		return map.get(word);
	}

	@Override
	public Set<String> getWords()
	{
		return new HashSet<String>(map.keySet());
	}
	
	private void mapOutTxt()
	{
		String txt= text.toLowerCase();
		List<String> words= createWords(txt);
		
		for(String word: words)
		{
			int instancesOfWord;
			
			if(map.get(word) == null)
			{
				instancesOfWord= 1;
			}
			else
			{
				instancesOfWord= map.get(word)+1;
			}
			map.put(word, instancesOfWord);
		}
	}
	
	private static List<String> createWords(String txt)
	{
		List<String> words= new ArrayList<>();
		String word= "";
		int length = txt.length();
		for(int i= 0; i<length; i++)
		{
			char letter= txt.charAt(i);
			if(Character.isLetterOrDigit(letter))
			{
				word+=letter;
			}
			if((Character.isWhitespace(letter) || i == length-1)  && !word.isEmpty())
			{
				words.add(word);
				word= "";
			}
		}
		return words;
	}

	@Override
	public int compareTo(Document o)
	{
		long comparison= this.getLastUseTime()- o.getLastUseTime();
		if(comparison > 0)
		{
			return 1;
		}
		if(comparison < 0)
		{
			return -1;
		}
		return 0;
	}

	@Override
	public long getLastUseTime()
	{
		return lastUsed;
	}

	@Override
	public void setLastUseTime(long timeInNanoseconds)
	{
		lastUsed= timeInNanoseconds;
	}
}
package edu.yu.cs.com1320.project.stage1.impl;

import java.net.URI;
import java.util.Arrays;

import edu.yu.cs.com1320.project.stage1.Document;

public class DocumentImpl implements Document
{
	private URI uri;
	private String text;
	private byte[] binaryData;
	
	public DocumentImpl(URI uri,String txt)
	{
		this.uri=uri;
		this.text=txt;
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
}
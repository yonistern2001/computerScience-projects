package eventManager;

import java.io.Serializable;

public class Worker implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private final String name, email, number;
	
	public Worker(String name, String number, String email)
	{
		this.name= name;
		this.email = email;
		this.number = number;
	}
	
	/**
	 * @return the name of the worker as a string 
	 */
	public String getName()
	{
		return name;
	}
	
	public String getPhoneNumber()
	{
		return number;
	}
	
	public String getEmail()
	{
		return email;
	}
	
	@Override
	public String toString()
	{
		return name;
	}
	
	@Override
	public boolean equals(Object o2)
	{
		if(!(o2 instanceof Worker))
			return false;
		Worker worker= (Worker) o2;
		return getName().equals(worker.getName());
	}
	
	@Override
	public int hashCode()
	{
		return name.hashCode();
	}
}
package eventManager;

import java.util.Calendar;

/**
 * compares based on which one has the later date
 */

public class Date implements Comparable<Date>, java.io.Serializable
{
	private static final long serialVersionUID = 1L;
	
	private  int minute, hour, month, day, year;
	private String periodOfDay;

	private Calendar calendar;

	public Date(int minute, int hour, String periodOfDay, int day, int month, int year)
	{
		this.calendar=Calendar.getInstance();
		periodOfDay= periodOfDay.toUpperCase();
		
		if(minute > 59 || minute < 0 || hour > 12 || hour <= 0 || day > 31 || day <= 0 || month > 12 || month <=0||year<this.calendar.getWeekYear())
		{
			throw new IllegalArgumentException();
		}
		if(!periodOfDay.equalsIgnoreCase("AM") && !periodOfDay.equalsIgnoreCase("PM"))
		{
			throw new IllegalArgumentException();
		}
		
		this.minute= minute;
		this.hour= hour;
		this.periodOfDay=periodOfDay.toUpperCase();
		this.day= day;
		this.month= month;
		this.year= year;
	}
	
	@Override
	public String toString()
	{
		return getDate();
	}
	
	/**
	 * @return the date and time as a String in hour:minute (am or pm) month/day/year
	 */
	public String getDate()
	{
		String output= hour+":";
		if(minute<10)
		{
			output+= "0";
		}
		output+= minute+" "+periodOfDay+"  "+month+"/"+day+"/"+year;
		return output;
	}

	@Override
	public int compareTo(Date o2)
	{
		int compare= this.year-o2.year;
		if(compare != 0)
		{
			return compare;
		}
		compare= this.month- o2.month;
		if(compare != 0)
		{
			return compare;
		}
		compare= this.day- o2.day;
		if(compare != 0)
		{
			return compare;
		}
		if(!this.periodOfDay.equals(o2.periodOfDay))
		{
			if(this.periodOfDay.equals("PM"))
			{
				return 1;
			}
			else
			{
				return -1;
			}
		}
		compare= this.hour- o2.hour;
		if(compare != 0)
		{
			return compare;
		}
		return this.minute- o2.minute;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if(!(o instanceof Date))
		{
			return false;
		}
		Date date= (Date) o;
		return getDate().equals(date.getDate());
	}
}
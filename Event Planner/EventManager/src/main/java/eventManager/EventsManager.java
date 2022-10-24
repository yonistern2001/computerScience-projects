package eventManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * keeps track of all events and stores workers in a database
 */
public class EventsManager 
{
	private static final String EVENTS_FILE_ADDRESS= "events.ser", 
			WORKERDATABASE_FILE_ADDRESS= "workersDatabase.ser";
	private HashMap<String, Event> events=new HashMap<>();
	private HashMap<String, Worker> workersDatabase= new HashMap<>();
	
	public EventsManager()
	{
		openSavedData();
	}
	
	public boolean deleteEvent(String event)
	{
		return events.remove(event) != null;
	}
	
	public List<Event> getEventsByLocation(String location)
	{
		List<Event> eventsByLocation= new ArrayList<>();
		for(Event event: events.values())
		{
			if(event.getLocation().equalsIgnoreCase(location))
			{
				eventsByLocation.add(event);
			}
		}
		eventsByLocation.sort(null);
		return eventsByLocation;
	}
	
	public List<Event> getEventsByTime(Date time)
	{
		List<Event> eventsByTime= new ArrayList<>();
		for(Event event: events.values())
		{
			if(event.getTimeAndDate().equals(time))
			{
				eventsByTime.add(event);
			}
		}
		eventsByTime.sort(null);
		return eventsByTime;
	}
	
	
	public Event getEventByName(String eventName)
	{
		return events.get(eventName);
	}

	public void addEvent(Event event)
	{
		String eventName = event.getEventName();
		if(events.containsKey(eventName))
		{
			throw new IllegalArgumentException("an event with this name was already added");
		}
		events.put(eventName, event);
	}
	
	public void addWorker(String name, String number, String email) throws IllegalArgumentException
	{
		if(workersDatabase.containsKey(name))
		{
			throw new IllegalArgumentException("name \""+name+"\" is already in the worker database");
		}
		workersDatabase.put(name, new Worker(name, number, email));
	}
	
	public List<Event> getAllEvents()
	{
		List<Event> output = new ArrayList<>(events.values());
		output.sort(null);
		return output;
	}
	
	public List<String> getAllEventNamesSorted()
	{
		List<String> output= new ArrayList<>();
		for(Event event: getAllEvents())
		{
			output.add(event.getEventName());
		}
		return output;
	}

	public List<Worker> getAllWorkers()
	{
		return new ArrayList<>(workersDatabase.values());
	}
	
	public Worker getWorkerByName(String name)
	{
		return workersDatabase.get(name);
	}
	
	public boolean hasWorker(String name)
	{
		return workersDatabase.containsKey(name);
	}
	
	public boolean removeWorker(String workerName)
	{
		return workersDatabase.remove(workerName) != null;
	}
	
	public void resetEventManager()
	{
		File file1= new File(EVENTS_FILE_ADDRESS), file2= new File(WORKERDATABASE_FILE_ADDRESS);
		file1.delete();
		file2.delete();
		workersDatabase = new HashMap<>();
		events = new HashMap<>();
	}
	
	@SuppressWarnings("unchecked")
	public void openSavedData()
	{
		File file1 = new File(EVENTS_FILE_ADDRESS), file2= new File(WORKERDATABASE_FILE_ADDRESS);
		if(!file1.exists() || !file2.exists())
			save();
		events= (HashMap<String, Event>) deserialize(EVENTS_FILE_ADDRESS);
		workersDatabase= (HashMap<String, Worker>) deserialize(WORKERDATABASE_FILE_ADDRESS);
	}
	
	private Object deserialize(String fileAddress)
	{
		FileInputStream fileIn;
		ObjectInputStream in;
		Object output= new HashMap<>();
		try
		{
			fileIn= new FileInputStream(fileAddress);
			in = new ObjectInputStream(fileIn);
			output= in.readObject();
			in.close();
			fileIn.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		return output;
	}

	public void save()
	{
		serialize(events, EVENTS_FILE_ADDRESS);
		serialize(workersDatabase, WORKERDATABASE_FILE_ADDRESS);
	}
	
	private void serialize(Object objectToSave, String fileAddress)
	{
		FileOutputStream fileOut;
		ObjectOutputStream out;
		try
		{
			fileOut = new FileOutputStream(fileAddress);
			out= new ObjectOutputStream(fileOut);
			out.writeObject(objectToSave);
			out.close();
			fileOut.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
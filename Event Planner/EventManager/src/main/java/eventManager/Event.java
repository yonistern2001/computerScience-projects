package eventManager;
import java.util.*;

public class Event implements Comparable<Event>, java.io.Serializable
{
	private static final long serialVersionUID = 1L;
	
	private final Date date;
	private final String location, name;
	private HashMap<String, Task> tasks;

	public Event(String eventName, String location, int minute, int hour, String periodOfDay, int day, int month, int year)
	{
		this.date= new Date(minute, hour, periodOfDay, day, month, year);
		this.location= location;
		this.name= eventName;
		this.tasks = new HashMap<>();
	}
	
	/**
	 * @return the location of the event as a string
	 */
	public String getLocation()
	{
		return this.location;
	}
	
	/**
	 * @return the Date object
	 */
	public Date getTimeAndDate()
	{
		return this.date;
	}
	
	public Set<Worker> getAllWorkers()
	{
		Set<Worker> workers= new HashSet<>();
		for(Task task: getAllTasksSorted())
		{
			workers.addAll(task.getWorkers());
		}
		return workers;
	}
	
	public Task getTaskByName(String name)
	{
		return this.tasks.get(name);
	}
	
	public void addTask(String name, String description, Priority priority)
	{
		this.tasks.put(name, new Task(name, description, priority));
	}
	
	public void removeTask(String task)
	{
		this.tasks.remove(task);
	}
	
	public List<Task> getAllTasksSorted()
	{
		ArrayList<Task> output = new ArrayList<>(this.tasks.values());
		output.sort(null);
		return output;
	}
	
	public List<String> getAllTaskNamesSorted()
	{
		ArrayList<String> names = new ArrayList<>();
		for(Task task: getAllTasksSorted())
		{
			names.add(task.getName());
		}
		return names;
	}
	
	public List<Task> getCompletedTasks()
	{
		List<Task> tasks= new ArrayList<>();
		for(Task task: this.tasks.values())
		{
			if(task.isComplete())
			{
				tasks.add(task);
			}
		}
		tasks.sort(null);
		return tasks;
	}
	
	public List<Task> getIncompleteTasks()
	{
		List<Task> tasks= new ArrayList<>();
		for(Task task: this.tasks.values())
		{
			if(!task.isComplete())
			{
				tasks.add(task);
			}
		}
		tasks.sort(null);
		return tasks;
	}
	
	/**
	 * @return true if all of the task have been completed and false if there are incomplete tasks
	 */
	public boolean allTasksComplete()
	{
		return getIncompleteTasks().isEmpty();
	}
	
	/**
	 * sort events by time and date
	 */
	@Override
	public int compareTo(Event o)
	{
		return date.compareTo(o.getTimeAndDate());
	}

	public String getEventName()
	{
		return name;
	}
	
	@Override
	public String toString()
	{
		return name;
	}
	
	@Override
	public boolean equals(Object o2)
	{
		if(!(o2 instanceof Task))
			return false;
		Event event= (Event) o2;
		return getEventName().equals(event.getEventName());
	}
}
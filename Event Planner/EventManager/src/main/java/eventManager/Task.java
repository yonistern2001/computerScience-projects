package eventManager;
import java.util.*;

public class Task implements Comparable<Task>, java.io.Serializable
{
	private static final long serialVersionUID = 1L;
	
	private boolean isComplete;
	private final String name, task;
	private Priority priority;
	private final Map<Worker, Double> salaries;

	public Task(String nameOfTask, String task, Priority priority)
	{
		this.task= task;
		this.name= nameOfTask;
		this.priority= priority;
		this.isComplete= false;
		this.salaries= new HashMap<>();
	}
	
	/**
	 * @param Boolean to mark this task as complete or incomplete
	 */
	public void setCompleted()
	{
		if(getWorkers().isEmpty())
		{
			throw new IllegalArgumentException("No worker Assigned");
		}
		this.isComplete=true;
	}
	
	public String getName()
	{
		return name;
	}

	/**
	 * 
	 * @return if the task is completed
	 * @throws IllegalAccessException
	 */
	public boolean isComplete()
	{
		return this.isComplete;
	}
	
	/**
	 * @return a string representation of what this task is
	 */
	public String getTask()
	{
		return this.task;
	}
	
	/**
	 * 
	 * @return the priority of this task as an enum
	 */
	public void changePriority(Priority priority)
	{
		this.priority=priority;
	}

	public Priority getPriority()
	{
		return this.priority;
	}
	
	/**
	 * @return a list of worker object
	 */
	public Set<Worker> getWorkers()
	{
		return this.salaries.keySet();
	}

	public void removeWorker(Worker worker)
	{
		if(!this.salaries.keySet().contains(worker))
		{
			throw new IllegalArgumentException("Worker is not found");
		}
		this.salaries.remove(worker);
	}
	
	/**
	 * @return the workers salary for this task
	 */
	public double getSalaryForWorker(Worker worker)
	{
		if(!this.salaries.keySet().contains(worker))
		{
			throw new IllegalArgumentException("Worker is not found");
		}
		return this.salaries.get(worker);
	}
	
	public String getSalaryForWorkerAsString(Worker worker)
	{
		Double salary = getSalaryForWorker(worker);
		if(salary%1==0)//whole
		{
			return "$"+salary.intValue() + ".00";
		}
		//not whole
		String text = Double.toString(Math.abs(salary));
		int integerPlaces = text.indexOf('.');
		if (integerPlaces + 2 >= text.length()) // tenths
		{
			return "$" + salary + "0";
		}
		else
		{
			return "$" + salary;
		}

	}
	
    /**
	 *pass in Worker object and assigns the worker to complete this specific task
	 *@param the Worker object and the amount of money they are making for completing this task
	 */
	public void assignWorker(Worker worker, double salary)
	{
		this.salaries.put(worker, salary);
	}

	/**
	 * task are compared first by if they were completed or not then by there priority
	 */
	@Override
	public int compareTo(Task o)
	{
		if(!(o.isComplete() == this.isComplete()))
		{
			if(this.isComplete())
			{
				return 1;
			}
			else
			{
				return -1;
			}
		}
		int priority1= this.getPriority().ordinal();
		int priority2= o.getPriority().ordinal();
		return priority2-priority1;
	}
	
	@Override
	public boolean equals(Object o2)
	{
		if(!(o2 instanceof Task))
			return false;
		Task task= (Task) o2;
		return getName().equals(task.getName());
	}
}

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

import eventManager.Priority;
import eventManager.Task;
import eventManager.Worker;

public class TaskTest
{
    private String taskString1, taskString2, taskString3, taskString4, taskString5;
    private String name1,name2,name3,name4,email1,email2,email3,email4,number1,number2,number3,number4;
    private Worker worker1, worker2, worker3, worker4;
    @SuppressWarnings("unused")
	private Task task1,task2,task3,task4,task5;
    @Before
    public void setup() 
    {
        this.taskString1="task number 1";
        this.taskString2="task number 2";
        this.taskString3="task number 3";
        this.taskString4="task number 4";
        this.taskString5="task number 5";
        this.task1=new Task("Task1",this.taskString1, Priority.GREEN);
        this.task2=new Task("Task2",this.taskString2, Priority.GREEN);
        this.task3=new Task("Task3",this.taskString3, Priority.YELLOW);
        this.task4=new Task("Task4",this.taskString4, Priority.YELLOW);
        this.task5=new Task("Task5",this.taskString5, Priority.RED);
        this.name1="Employee1";
        this.name2="Employee2";
        this.name3="Employee3";
        this.name4="Employee4";
        this.email1="Employee1@ncsy.org";
        this.email2="Employee2@ncsy.org";
        this.email3="Employee3@ncsy.org";
        this.email4="Employee4@ncsy.org";
        this.number1="1";
        this.number2="2";
        this.number3="3";
        this.number4="4";
        this.worker1= new Worker(this.name1, this.number1, this.email1);
        this.worker2= new Worker(this.name2, this.number2, this.email2);
        this.worker3= new Worker(this.name3, this.number3, this.email3);
        this.worker4= new Worker(this.name4, this.number4, this.email4);
    }

    @Test
    public void testCompleted() //should throw as no worker was assigned
    {
        boolean caught = false;
        try{
            this.task1.setCompleted();
        }
        catch(IllegalArgumentException e)
        {
            caught=true;
        }
        assert caught;
       
        this.task1.assignWorker(this.worker1, 10);
        this.task1.setCompleted();
        assert this.task1.isComplete();
    }

    @Test
    public void testGetTask() 
    {
        assertEquals(this.taskString1, task1.getTask());
    }

    @Test
    public void priority() 
    {
        assertEquals(Priority.GREEN, task1.getPriority());
        task1.changePriority(Priority.RED);
        assertEquals(Priority.RED, task1.getPriority());
    }

    @Test
    public void testWorkers() 
    {
        this.task1.assignWorker(this.worker1, 10);
        this.task1.assignWorker(this.worker2, 20);
        assertFalse(this.task1.isComplete());
        HashSet<Worker> workers = new HashSet<>();
        workers.add(this.worker1);
        workers.add(this.worker2);
        assertEquals(workers, this.task1.getWorkers());
        assert(10 ==this.task1.getSalaryForWorker(this.worker1));
        boolean caught=false;
        try {
            this.task1.removeWorker(this.worker3);
        } catch (Exception e) {
            caught=true;
        }
        assert caught;
        this.task1.removeWorker(this.worker1);
        workers.remove(this.worker1);
        assertEquals(workers, this.task1.getWorkers());
    }
    
    @Before
    public void setup2() 
    {
        this.taskString1="task number 1";
        this.taskString2="task number 2";
        this.taskString3="task number 3";
        this.taskString4="task number 4";
        this.taskString5="task number 5";
        this.task1=new Task("Task1",this.taskString1, Priority.GREEN);
        this.task2=new Task("Task2",this.taskString2, Priority.GREEN);
        this.task3=new Task("Task3",this.taskString3, Priority.YELLOW);
        this.task4=new Task("Task4",this.taskString4, Priority.YELLOW);
        this.task5=new Task("Task5",this.taskString5, Priority.RED);
        this.name1="Employee1";
        this.name2="Employee2";
        this.name3="Employee3";
        this.name4="Employee4";
        this.email1="Employee1@ncsy.org";
        this.email2="Employee2@ncsy.org";
        this.email3="Employee3@ncsy.org";
        this.email4="Employee4@ncsy.org";
        this.number1="1";
        this.number2="2";
        this.number3="3";
        this.number4="4";
        this.worker1= new Worker(this.name1, this.number1, this.email1);
        this.worker2= new Worker(this.name2, this.number2, this.email2);
        this.worker3= new Worker(this.name3, this.number3, this.email3);
        this.worker4= new Worker(this.name4, this.number4, this.email4);
    }
    @Test
    public void testCompare() 
    {
        task1.assignWorker(this.worker1, 20);
        task2.assignWorker(this.worker2, 20);
        task3.assignWorker(this.worker3, 20);
        task5.assignWorker(this.worker4, 20);
        assertEquals(1, this.task1.compareTo(this.task3));
        //this.task2.changePriority(Priority.GREEN);
       // this.task1.changePriority(Priority.GREEN);
        assertEquals(0, this.task1.compareTo(this.task2));
        assertEquals(-1, this.task5.compareTo(this.task3));
    }









}

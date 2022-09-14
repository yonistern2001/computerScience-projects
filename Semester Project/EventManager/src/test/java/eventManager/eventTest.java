package eventManager;
import eventManager.Task;
import eventManager.Worker;
import eventManager.Priority;
import org.junit.Before;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import org.junit.*;
import org.junit.Assert.*;

public class eventTest 
{
    private String taskString1, taskString2, taskString3, taskString4, taskString5;
    //private String name1,name2,name3,name4,email1,email2,email3,email4,number1,number2,number3,number4;
    //private Worker worker1, worker2, worker3, worker4;
    private Task task1,task2,task3,task4,task5;
    private Event event1;
    private String name1,name2,name3,name4,email1,email2,email3,email4,number1,number2,number3,number4;
    private Worker worker1, worker2, worker3, worker4;
    @Before
    public void setupWorkers() 
    {
        this.name1="Employee1";
        this.name2="Amployee2";
        this.name3="Employee3";
        this.name4="Employee4";
        this.email1="Employee1@ncsy.org";
        this.email2="Employee2@ncsy.org";
        this.email3="Employee3@ncsy.org";
        this.email4="Employee4@ncsy.org";
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
        this.number1="1";
        this.number2="2";
        this.number3="3";
        this.number4="4";
        this.worker1= new Worker(this.name1, this.number1, this.email1);
        this.worker2= new Worker(this.name2, this.number2, this.email2);
        this.worker3= new Worker(this.name3, this.number3, this.email3);
        this.worker4= new Worker(this.name4, this.number4, this.email4);
    }

    @Before
    public void setup() 
    {
        this.event1= new Event("Regional", "Albany", 00, 11, "am", 18, 5, 2022);

    }

    @Test
    public void testDelete() 
    {
        EventsManager eventManager = new EventsManager();
        eventManager.resetEventManager();
        eventManager.addEvent(this.event1);
        eventManager.save();
        //assertTrue(eventManager.returnFile().length()>0);
        //System.out.println(eventManager.returnFile().length());
        //eventManager.close();       
        //eventManager.deleteEvent(this.event1);
        //eventManager.save();
        //System.out.println(eventManager.returnFile().length());
        //eventManager.close();        assertTrue(eventManager.returnFile().length()==0);
        //assertEquals(Collections.emptyList(), eventManager.getAllEvents());
    }

    @Test
    public void testDeletePart2() 
    {
        EventsManager eventManager = new EventsManager();
        eventManager.deleteEvent(this.event1.getEventName());
        eventManager.save();
        assertEquals(Collections.emptyList(), eventManager.getAllEvents());
    }
    @Test
    public void testDeletePart3() 
    {
        EventsManager eventManager = new EventsManager();
        assertEquals(Collections.emptyList(), eventManager.getAllEvents());
    }

    @Test
    public void testDeletePart4() 
    {
        EventsManager eventManager = new EventsManager();
        //eventManager.resetEventManager();
        eventManager.addEvent(this.event1);
        eventManager.save();
        //assertTrue(eventManager.returnFile().length()>0);
        //System.out.println(eventManager.returnFile().length());
        //eventManager.close();       
        //eventManager.deleteEvent(this.event1);
        //eventManager.save();
        //System.out.println(eventManager.returnFile().length());
        //eventManager.close();        assertTrue(eventManager.returnFile().length()==0);
        //assertEquals(Collections.emptyList(), eventManager.getAllEvents());
    }

    @Test
    public void testWorkers() 
    {
        ArrayList<String> workers = new ArrayList<>();
        EventsManager eventManager = new EventsManager();
        eventManager.addWorker(this.name1, this.number1, this.email1);
        eventManager.addWorker(this.name2, this.number2, this.email2);
        eventManager.addWorker(this.name3, this.number3, this.email3);
        eventManager.addWorker(this.name4, this.number4, this.email4);
        workers.add(this.name2);
        workers.add(this.name1);
        workers.add(this.name3);
        workers.add(this.name4);
        assertEquals(workers, eventManager.getAllWorkers());
    }

    @Test
    public void testGetTasks() 
    {
        ArrayList<Task> task = new ArrayList<>();
        EventsManager eventManager = new EventsManager();
        eventManager.resetEventManager();
        eventManager.addWorker(this.name1, this.number1, this.email1);
        eventManager.addWorker(this.name2, this.number2, this.email2);
        eventManager.addWorker(this.name3, this.number3, this.email3);
        eventManager.addWorker(this.name4, this.number4, this.email4);
        this.event1.addTask("Task1",this.taskString1, Priority.GREEN);
        this.event1.addTask("Task2",this.taskString2, Priority.GREEN);
        this.event1.addTask("Task5",this.taskString5, Priority.RED);
        this.event1.addTask("Task3",this.taskString3, Priority.YELLOW);
        eventManager.addEvent(this.event1);
        task.add(this.task1);
        task.add(this.task2);
        task.add(this.task3);
        task.add(this.task5);
       /*for(Task e:eventManager.getAllIncompleteTasks(this.event1.getEventName()))
       {
           System.out.println(e.getName());
       }*/
    }


}




import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import eventManager.Worker;
public class WorkerTest 
{
    private String name1,name2,name3,name4,email1,email2,email3,email4,number1,number2,number3,number4;
    private Worker worker1, worker2, worker3, worker4;
    @Before
    public void setup() 
    {
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
    public void testGetName() 
    {
        assertEquals(this.name1, this.worker1.getName());
        assertEquals(this.name2, this.worker2.getName());
        assertEquals(this.name3, this.worker3.getName());
        assertEquals(this.name4, this.worker4.getName());
    }

    @Test
    public void testGetEmail() 
    {
        assertEquals(this.email1, this.worker1.getEmail());
        assertEquals(this.email2, this.worker2.getEmail());
        assertEquals(this.email3, this.worker3.getEmail());
        assertEquals(this.email4, this.worker4.getEmail());
    }

    @Test
    public void testGetNumber() 
    {
        assertEquals(this.number1, this.worker1.getPhoneNumber());
        assertEquals(this.number2, this.worker2.getPhoneNumber());
        assertEquals(this.number3, this.worker3.getPhoneNumber());
        assertEquals(this.number4, this.worker4.getPhoneNumber());
    }
    /*@Test
    public void testUpdate() 
    {
        assertEquals(this.email1, this.worker1.getEmail());
        worker1.updateEmail("Jonathan@NCSY.org");
        assertEquals("Jonathan@NCSY.org", this.worker1.getEmail());
        worker2.updatePhoneNumber("5432");
        assertEquals("5432", worker2.getPhoneNumber());
    }

    @Test
    public void testEquals() 
    {
        Worker clone = new Worker(this, this.number1, this.email1);//seeming exact clone
        assertFalse(worker1.equals(clone));
    }*/
}

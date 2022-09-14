import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import edu.yu.cs.com1320.project.Stack;
import edu.yu.cs.com1320.project.impl.StackImpl;

public class StackTest
{
	Stack<String> stack= new StackImpl<>();
	String A= "A";
	String B= "B";
	String C= "C";
	String D= "D";
	
	@Test
	public void pushAndPopStack()
	{
		stack.push(A);
		stack.push(B);
		stack.push(C);
		stack.push(D);
		assertEquals(D, stack.pop());
		assertEquals(C, stack.pop());
		assertEquals(B, stack.pop());
		assertEquals(A, stack.pop());
	}
	
	@Test
	public void stackTest()
	{
		assertNull(stack.peek());
		stack.push(A);
		assertEquals(1, stack.size());
		assertEquals(A, stack.peek());
		stack.push(B);
		assertEquals(B, stack.pop());
		assertEquals(A, stack.peek());
		stack.push(C);
		stack.push(D);
		assertEquals(D, stack.peek());
		assertEquals(3, stack.size());
		stack.push(B);
		assertEquals(B, stack.pop());
		assertEquals(D, stack.pop());
		assertEquals(C, stack.pop());
		assertEquals(A, stack.pop());
		assertEquals(0, stack.size());
		assertNull(stack.pop());
	}
}

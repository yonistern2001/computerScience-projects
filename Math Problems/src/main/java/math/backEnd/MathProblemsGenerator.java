package math.backEnd;

import java.util.*;

public class MathProblemsGenerator
{
	public static void main(String[] args)
	{
		Scanner console=new Scanner(System.in);
		System.out.println("Enter the operation that you want to use: Addition, Subtraction, Multiplication or Division");
		String operation=console.next();
		System.out.println("Type the number of questions that you want to be generated");
		int times=console.nextInt();
		MathProblemsGenerator i=new MathProblemsGenerator(times, getOperation(operation));
		while(i.hasNextProblem())
		{
			System.out.println(i.getCurrentQuestion());
			int answer= console.nextInt();
			answer=i.getCurrentCorrectAnswer(answer);
			if(answer==-1)
			{
				System.out.println("Correct Answer");
			}
			else
			{
				System.out.println("Wrong Answer: the correct answer was "+answer);
			}
		}
		console.close();
		System.out.println("\nCorrect Answers: "+i.getNumOfCorrectAnswers()+"/"+times);
	}
	
	public static Operation getOperation(String input)
	{
		input= input.toLowerCase();
		switch(input)
		{
			case "addition":
				return Operation.ADDITION;
			case "subtraction":
				return Operation.SUBTRACTION;
			case "multiplication":
				return Operation.MULTIPLICATION;
			case "division":
				return Operation.DIVISION;
			default:
				return null;
		}
	}

	private final int timesToRun;
	List<MathProblem> mathProblems;
	private int correctAnswers=0;
	private int position=0;
	
	public MathProblemsGenerator(int timesToRun, Operation operation)
	{
		this.timesToRun=timesToRun;
		setTimesToRun(operation);
	}
	
	private void setTimesToRun(Operation operation)
	{
		mathProblems=new ArrayList<>(timesToRun);
		for(int i=0;i<timesToRun;i++)
		{
			mathProblems.add(createMathProblemObject(operation));
		}
	}
	
	public MathProblem getNextMathProblem()
	{
		MathProblem problem= mathProblems.get(position);
		position+=1;
		return problem;
	}
	
	public Boolean hasNextProblem()
	{
		if(position>=timesToRun)
		{
			return false;
		}
		return true;
	}
	
	public int getNumOfCorrectAnswers()
	{
		return correctAnswers;
	}
	
	public int getCurrentCorrectAnswer(int inputAnswer)
	{
		int answer= getNextMathProblem().getAnswer();
		if(answer==inputAnswer)
		{
			correctAnswers+=1;
			return -1;
		}
		else
		{
			return answer;
		}
	}
	
	public String getCurrentQuestion()
	{
		return mathProblems.get(position).getQuestion();
	}
	
	public MathProblem createMathProblemObject(Operation operationType)
	{
		switch (operationType)
		{
		case ADDITION: 
			return new AdditionProblem();
		case SUBTRACTION:
			return new SubtractionProblem();
		case MULTIPLICATION:
			return new MultiplicationProblem();
		case DIVISION:
			return new DivisionProblem();
		}
		return null;
	}
}
package math.backEnd;

public class DivisionProblem extends MathProblem
{
	private int number1;
	private int number2;
	private final int answer;
	
	public DivisionProblem()
	{
		number1=num1*num2;
		if(number1==0)
		{
			number1=(int)(Math.random()*(super.MAX_NUMBER-1))+1;
			number2=0;
			answer=0;
		}
		else
		{
			number2=num1;
			answer=num2;
		}
	}
	
	@Override
	public int getAnswer() {
		return answer;
	}

	@Override
	public String getQuestion()
	{
		return number1+"\u00F7"+number2+"=";
	}

}

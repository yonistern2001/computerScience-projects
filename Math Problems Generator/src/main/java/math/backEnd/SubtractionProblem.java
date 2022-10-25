package math.backEnd;

public class SubtractionProblem extends MathProblem
{
	private final int number1;
	private final int number2;
	
	public SubtractionProblem()
	{
		if(num1>num2)
		{
			number1=num1;
			number2=num2;
		}
		else
		{
			number1=num2;
			number2=num1;
		}
	}
	
	@Override
	public int getAnswer()
	{
		return number1-number2;
	}

	@Override
	public String getQuestion()
	{
		return (String)(number1+"-"+number2+"=");
	}
}
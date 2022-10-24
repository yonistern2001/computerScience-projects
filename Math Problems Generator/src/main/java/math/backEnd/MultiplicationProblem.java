package math.backEnd;

public class MultiplicationProblem extends MathProblem
{
	@Override
	public int getAnswer()
	{
		return num1*num2;
	}

	@Override
	public String getQuestion() {
		return (String)(num1+"x"+num2+"=");
	}
	
}

package math.backEnd;

public abstract class MathProblem
{
	public final int MAX_NUMBER=12;
	public final int num1=(int)(Math.random()*MAX_NUMBER);
	public final int num2=(int)(Math.random()*MAX_NUMBER);

	abstract int getAnswer();
	
	abstract String getQuestion();
}

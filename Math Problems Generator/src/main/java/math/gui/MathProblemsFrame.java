package math.gui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import math.backEnd.MathProblemsGenerator;
import math.backEnd.Operation;

public class MathProblemsFrame extends JFrame
{
	private static final long serialVersionUID = 1L;

	public static void main(String[]args)
	{
		JFrame frame= new MathProblemsFrame(10, Operation.MULTIPLICATION, 40);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
	
	private MathProblemsGenerator mathGenerator;
	private JTextField answerFeild;
	private JButton nextButton;
	private JLabel answerLabel, timeRemainingLabel, correctAnswersLabel, numQuestionsLeftLabel, problemLabel;
	private boolean nextQuestion= false;
	private final int numOfQuestions;
	private Timer timer;
	private int timeRemaining, questionsAnswered;
	
	public MathProblemsFrame(int numOfQuestions, Operation operation, int timerLength)
	{
		this.setTitle("Math Poblems");
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setPreferredSize(new Dimension(355, 500));
		Container pane = this.getContentPane();
		this.setLayout(new GridLayout(5, 1, 0, 30));
		((JComponent) pane).setBorder(new CompoundBorder(new EtchedBorder(80), new EmptyBorder(20, 15, 12, 12)));
		this.timeRemaining= timerLength;
		this.numOfQuestions= numOfQuestions;
		mathGenerator= new MathProblemsGenerator(numOfQuestions, operation);
		
		add(createQuestionPanel());
		
		JPanel stats= new JPanel();
		correctAnswersLabel= new JLabel(getNumOfCorrectAndWrongAnswers());
		numQuestionsLeftLabel= new JLabel(getNumOfQuestionsRemaining());
		numQuestionsLeftLabel.setFont(new Font("Dialog", Font.PLAIN, 15));
		correctAnswersLabel.setFont(new Font("Dialog", Font.PLAIN, 15));
		stats.add(correctAnswersLabel);
		stats.add(numQuestionsLeftLabel);
		this.add(stats);
		
		if(timerLength != 0)
		{
			addTimer();
		}
		
		answerLabel= new JLabel();
		answerLabel.setFont(new Font("Tahoma", Font.PLAIN, 16));
		answerLabel.setVisible(false);
		add(answerLabel);
		
		nextButton= new JButton("Next");
		nextButton.setFocusPainted(false);
		nextButton.setFont(new Font("Dialog", Font.PLAIN, 24));
		nextButton.addActionListener(new NextListener());
		add(nextButton);
	}

	private void addTimer()
	{
		timeRemainingLabel= new JLabel(getTimeRemainingAsString());
		timeRemainingLabel.setFont(new Font("Dialog", Font.PLAIN, 15));
		add(timeRemainingLabel);
		timer= new Timer(1000, new TimeListener());
		timer.start();
	}

	private String getTimeRemainingAsString()
	{
		return "Time remaining: "+timeRemaining--;
	}
	
	private String getNumOfCorrectAndWrongAnswers()
	{
		int numOfCorrectAnswers= mathGenerator.getNumOfCorrectAnswers();
		return "Correct answers: "+numOfCorrectAnswers+"  Wrong answers: "+((questionsAnswered++)-numOfCorrectAnswers);
	}
	
	private String getNumOfQuestionsRemaining()
	{
		int remainingQuestions= numOfQuestions- questionsAnswered;
		String output= "Questions remaining: ";
		if(remainingQuestions == -1)
		{
			output+= 0;
		}
		else
		{
			output+= remainingQuestions;
		}
		return output;
	}

	private JPanel createQuestionPanel()
	{
		JPanel panel= new JPanel();
		problemLabel= new JLabel(mathGenerator.getCurrentQuestion());
		answerFeild= new JTextField(7);
		problemLabel.setFont(new Font("Tahoma", Font.PLAIN, 30));
		answerFeild.setFont(new Font("Tahoma", Font.PLAIN, 24));
		answerFeild.addActionListener(new NextListener());
		panel.add(problemLabel);
		panel.add(answerFeild);
		panel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
		return panel;
	}
	
	private void endGame()
	{
		JOptionPane.showMessageDialog(MathProblemsFrame.this, "game over\nyou got "+mathGenerator.getNumOfCorrectAnswers()+" out of "+numOfQuestions+" correct");
		MathProblemsFrame.this.dispose();
	}
	
	private class TimeListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			if(timeRemaining == -1)
			{
				timer.stop();
				MathProblemsFrame.this.endGame();
			}
			timeRemainingLabel.setText(MathProblemsFrame.this.getTimeRemainingAsString());
		}
	}
	
	private class NextListener implements ActionListener
	{
		@Override
	    public void actionPerformed(ActionEvent e)
		{
			if(mathGenerator.hasNextProblem())
			{
				if(!nextQuestion)
				{
					answerLabel.setText(getAnswer());
					if(answerLabel.getText().length() != 27)
					{
						correctAnswersLabel.setText(MathProblemsFrame.this.getNumOfCorrectAndWrongAnswers());
						numQuestionsLeftLabel.setText(MathProblemsFrame.this.getNumOfQuestionsRemaining());
					}
				}
				else
				{
					problemLabel.setText(mathGenerator.getCurrentQuestion());
					answerFeild.setText("");
				}
				answerLabel.setVisible(!nextQuestion);
				answerFeild.setEditable(nextQuestion);
				nextQuestion= !nextQuestion;
			}
			else
			{
				MathProblemsFrame.this.endGame();
			}
		}

		private String getAnswer()
		{
			String stringAnswer= answerFeild.getText();
			int answer;
			try
			{
				answer= Integer.parseInt(stringAnswer);
			}
			catch(NumberFormatException i)
			{
				return "please enter a valid number";
			}
			int output= mathGenerator.getCurrentCorrectAnswer(answer);
			if(output==-1)
			{
				return "Correct Answer!";
			}
			else
			{
				return "Incorrect Answer, the correct answer is "+output;
			}
		}
	}
}
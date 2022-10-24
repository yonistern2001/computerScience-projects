package math.gui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import math.backEnd.*;

import javax.swing.border.*;

public class RunMathProblemsFrame extends JFrame
{
	private static final long serialVersionUID = 1L;

	public static void main(String[] args)
	{
		RunMathProblemsFrame panel= new RunMathProblemsFrame();
		panel.setVisible(true);
	}
	
	@SuppressWarnings("rawtypes")
	private JComboBox operations,numOfProblems, timerLength;
	
	@SuppressWarnings("unchecked")
	public RunMathProblemsFrame()
	{
		((JComponent) this.getContentPane()).setBorder(new CompoundBorder(new EtchedBorder(80), new EmptyBorder(30, 15, 12, 12)));
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setTitle("Settings");
		this.setBounds(100, 100, 355, 500);
		this.setLocationRelativeTo(null);
		this.setLayout(new GridLayout(4, 1, 0, 30));
		
		JPanel[] panels= createPanels();
		
		addLabels(panels);
		
		this.operations = new JComboBox<>();
		operations.setFont(new Font("Tahoma", Font.PLAIN, 18));
		operations.setModel(new DefaultComboBoxModel<>(Operation.values()));
		panels[0].add(operations);
		
		this.numOfProblems = new JComboBox<>();
		numOfProblems.setFont(new Font("Tahoma", Font.PLAIN, 18));
		numOfProblems.setModel(new DefaultComboBoxModel<>(createNums(1, 50)));
		panels[1].add(numOfProblems);
		
		
		this.timerLength = new JComboBox<>();
		timerLength.setFont(new Font("Tahoma", Font.PLAIN, 18));
		String[] times = createNums(9, 250);
		times[0]= "No Timer";
		timerLength.setModel(new DefaultComboBoxModel<>(times));
		panels[2].add(timerLength);
		
		JButton startButton= new JButton("Start");
		startButton.setBackground(Color.WHITE);
		startButton.setFont(new Font("Sans Serif", Font.PLAIN, 25));
		startButton.setFocusPainted(false);
		startButton.addActionListener(new StartButtonActionListener());
		panels[3].add(startButton);
	}

	private String[] createNums(int i, int j)
	{
		int length= j-i+1;
		String[] output= new String[length];
		for(int a= 0; a < length; a++)
		{
			output[a]= String.valueOf(a+i);
		}
		return output;
	}

	private void addLabels(JPanel[] panels)
	{
		JLabel label1= new JLabel("Select operation  "), label2= new JLabel("Select number of problems "), label3= new JLabel("Select timer length ");
		label1.setFont(new Font("Tahoma", Font.PLAIN, 20));
		label2.setFont(new Font("Tahoma", Font.PLAIN, 20));
		label3.setFont(new Font("Tahoma", Font.PLAIN, 20));

		panels[0].add(label1);
		panels[1].add(label2);
		panels[2].add(label3);
	}

	private JPanel[] createPanels()
	{
		JPanel[] panels= new JPanel[4];
		for(int i=0; i<4; i++)
		{
			JPanel curr= new JPanel();
			if(i != 3)
			{
				curr.setBorder(new EtchedBorder(1000));
			}
			this.add(curr);
			panels[i]= curr;
		}
		return panels;
	}
	
	class StartButtonActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			int timer= timerLength.getSelectedIndex();
			if(timer != 0)
			{
				timer+=9;
			}
			JFrame frame= new MathProblemsFrame(numOfProblems.getSelectedIndex()+1, (Operation) operations.getSelectedItem(), timer);
			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
			RunMathProblemsFrame.this.dispose();
		}
	}
}
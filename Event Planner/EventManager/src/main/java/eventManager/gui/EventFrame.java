package eventManager.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import eventManager.Event;
import eventManager.EventsManager;
import eventManager.Task;

public class EventFrame extends JFrame
{
	private static final long serialVersionUID = 1L;
	
	private JPanel contentPane;
	private Event event;
	private JButton deleteButton;
	private EventsManager manager;
	private ButtonsPanel events;

	private JButton addTaskButton;
	private ButtonsPanel tasksPanel;

	public EventFrame(Event event, EventsManager manager, ButtonsPanel events)
	{
		setTitle("Event Veiwer");
		this.events= events;
		this.manager= manager;
		this.event= event;
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 450, 670);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel eventNameLabel = new JLabel(event.getEventName());
		eventNameLabel.setFont(new Font("Tahoma", Font.PLAIN, 22));
		eventNameLabel.setBounds(31, 20, 395, 49);
		contentPane.add(eventNameLabel);
		
		this.deleteButton = new JButton("Delete");
		deleteButton.addActionListener(new Listener());
		deleteButton.setFont(new Font("Tahoma", Font.BOLD, 20));
		deleteButton.setForeground(Color.RED);
		deleteButton.setBounds(139, 585, 143, 38);
		contentPane.add(deleteButton);
		
		this.tasksPanel = new ButtonsPanel(event.getAllTaskNamesSorted(), new ButtonListener());
		tasksPanel.setBounds(31, 288, 365, 228);
		contentPane.add(tasksPanel);
		
		JLabel tasksLabel = new JLabel("Tasks");
		tasksLabel.setFont(new Font("Tahoma", Font.PLAIN, 20));
		tasksLabel.setBounds(42, 253, 72, 36);
		contentPane.add(tasksLabel);
		
		JLabel locationLabel = new JLabel("Location: "+event.getLocation());
		locationLabel.setFont(new Font("Tahoma", Font.PLAIN, 18));
		locationLabel.setBounds(42, 113, 366, 30);
		contentPane.add(locationLabel);
		
		JLabel timeLabel = new JLabel("Time: "+event.getTimeAndDate().toString());
		timeLabel.setFont(new Font("Tahoma", Font.PLAIN, 18));
		timeLabel.setBounds(42, 194, 366, 30);
		contentPane.add(timeLabel);
		
		this.addTaskButton = new JButton("Add Task");
		addTaskButton.addActionListener(new Listener());
		addTaskButton.setFont(new Font("Tahoma", Font.PLAIN, 18));
		addTaskButton.setBounds(31, 537, 136, 24);
		contentPane.add(addTaskButton);
	}
	
	class Listener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			Object source= e.getSource();
			if(source == addTaskButton)
			{
				JFrame frame= new TaskCreatorFrame(manager, event, tasksPanel);
				frame.pack();
				frame.setSize(450, 670);
				frame.setVisible(true);
			}
			else if(source == deleteButton)
			{
				int result= JOptionPane.showConfirmDialog(rootPane, "Are you sure you want to delete this event");
				if(result != JOptionPane.YES_OPTION)
					return;
				manager.deleteEvent(event.getEventName());
				manager.save();
				events.refresh(manager.getAllEventNamesSorted());
				EventFrame.this.dispose();
			}
		}
	}
	
	private class ButtonListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			JButton source= (JButton) e.getSource();
			String eventName= source.getText();
			Task task= event.getTaskByName(eventName);
			System.out.println(task.getName()+" "+task.getTask()+" "+task.getPriority().toString());
			JFrame frame= new TaskFrame(task, manager, event, tasksPanel);
			frame.pack();
			frame.setSize(450, 670);
			frame.setVisible(true);
		}
	}
}
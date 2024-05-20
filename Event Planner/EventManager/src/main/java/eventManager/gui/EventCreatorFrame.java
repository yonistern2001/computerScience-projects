package eventManager.gui;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import eventManager.Event;
import eventManager.EventsManager;

public class EventCreatorFrame extends JFrame
{
	private static final long serialVersionUID = 1L;
	
	private JPanel contentPane;
	private JTextField eventTextField;
	private JTextField eventLocationTextFeild;
	private JComboBox<String> hour, minute, amOrPm, day, month, year;
	private JButton createEventButton;
	private EventsManager manager;
	private ButtonsPanel events;

	public EventCreatorFrame(EventsManager manager, ButtonsPanel events)
	{
		setAlwaysOnTop(true);
		this.events= events;
		this.manager= manager;
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		setTitle("Event Creator");
		setBounds(100, 100, 450, 670);
		getContentPane().setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel.setBounds(53, 62, 319, 89);
		getContentPane().add(panel);
		panel.setLayout(null);
		
		eventTextField = new JTextField();
		eventTextField.setFont(new Font("Tahoma", Font.PLAIN, 13));
		eventTextField.setBounds(10, 45, 253, 34);
		panel.add(eventTextField);
		eventTextField.setColumns(10);
		
		JLabel eventNameLabel = new JLabel("Event Name");
		eventNameLabel.setBounds(10, 0, 280, 45);
		panel.add(eventNameLabel);
		eventNameLabel.setFont(new Font("Tahoma", Font.PLAIN, 20));
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel_1.setLayout(null);
		panel_1.setBounds(53, 179, 319, 89);
		getContentPane().add(panel_1);
		
		eventLocationTextFeild = new JTextField();
		eventLocationTextFeild.setFont(new Font("Tahoma", Font.PLAIN, 13));
		eventLocationTextFeild.setColumns(10);
		eventLocationTextFeild.setBounds(10, 45, 253, 34);
		panel_1.add(eventLocationTextFeild);
		
		JLabel eventLocationLabel = new JLabel("Event Location");
		eventLocationLabel.setFont(new Font("Tahoma", Font.PLAIN, 20));
		eventLocationLabel.setBounds(10, 0, 299, 45);
		panel_1.add(eventLocationLabel);
		
		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel_2.setLayout(null);
		panel_2.setBounds(53, 319, 319, 136);
		getContentPane().add(panel_2);
		
		JLabel eventTimeLabel = new JLabel("Select the date of this event");
		eventTimeLabel.setFont(new Font("Tahoma", Font.PLAIN, 20));
		eventTimeLabel.setBounds(10, 0, 265, 45);
		panel_2.add(eventTimeLabel);
		
		this.hour = new JComboBox<>();
		hour.setModel(new DefaultComboBoxModel<>(createArrayWithNums("Hour", 12)));
		hour.setFont(new Font("Tahoma", Font.PLAIN, 13));
		hour.setBounds(10, 43, 55, 36);
		panel_2.add(hour);
		
		this.minute = new JComboBox<>();
		minute.setModel(new DefaultComboBoxModel<>(createArrayWithNums("Minute", 60)));
		minute.setFont(new Font("Tahoma", Font.PLAIN, 13));
		minute.setBounds(103, 43, 70, 36);
		panel_2.add(minute);
		
		this.amOrPm = new JComboBox<>();
		amOrPm.setModel(new DefaultComboBoxModel<>(new String[] {"AM", "PM"}));
		amOrPm.setFont(new Font("Tahoma", Font.PLAIN, 13));
		amOrPm.setBounds(231, 43, 44, 36);
		panel_2.add(amOrPm);
		
		this.day = new JComboBox<>();
		day.setModel(new DefaultComboBoxModel<>(createArrayWithNums("Day", 31)));
		day.setFont(new Font("Tahoma", Font.PLAIN, 13));
		day.setBounds(10, 90, 55, 36);
		panel_2.add(day);
		
		this.month = new JComboBox<>();
		month.setModel(new DefaultComboBoxModel<>(createArrayWithNums("Month", 12)));
		month.setFont(new Font("Tahoma", Font.PLAIN, 13));
		month.setBounds(103, 90, 70, 36);
		panel_2.add(month);
		
		
		String[] years = createYearsArray();
		
		year = new JComboBox<>();
		year.setModel(new DefaultComboBoxModel<>(years));
		year.setFont(new Font("Tahoma", Font.PLAIN, 13));
		year.setBounds(205, 89, 70, 36);
		panel_2.add(year);
		
		this.createEventButton = new JButton("Create Event");
		createEventButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
		createEventButton.addActionListener(new Listener());
		createEventButton.setBounds(140, 525, 137, 31);
		getContentPane().add(createEventButton);
	}

	private String[] createYearsArray() {
		String[] years= new String[101];
		years[0]= "Years";
		for(int i=1; i<=100; i++)
		{
			years[i]= ""+(i+2021);
		}
		return years;
	}
	
	private static String[] createArrayWithNums(String message, int upTo)
	{
		String[] output= new String[upTo+1];
		output[0]= message;
		for(int i=1; i<=upTo; i++)
		{
			output[i]= ""+i;
		}
		return output;
	}
	
	class Listener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			String name= eventTextField.getText();
			String location= eventLocationTextFeild.getText();
			int minute= EventCreatorFrame.this.minute.getSelectedIndex();
			int hour= EventCreatorFrame.this.hour.getSelectedIndex();
			int day= EventCreatorFrame.this.day.getSelectedIndex();
			int month= EventCreatorFrame.this.month.getSelectedIndex();
			int year= EventCreatorFrame.this.year.getSelectedIndex()+2021;
			String amOrPm;
			if(EventCreatorFrame.this.amOrPm.getSelectedIndex() == 0)
			{
				amOrPm= "am";
			}
			else
			{
				amOrPm= "pm";
			}
			
			Event event= new Event(name, location, minute, hour, amOrPm, day, month, year);
			manager.addEvent(event);
			manager.save();
			events.refresh(manager.getAllEventNamesSorted());
			EventCreatorFrame.this.dispose();
		}
	}
}

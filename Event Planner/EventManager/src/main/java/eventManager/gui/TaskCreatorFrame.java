package eventManager.gui;

import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import eventManager.Event;
import eventManager.EventsManager;
import eventManager.Priority;

public class TaskCreatorFrame extends JFrame
{
	private static final long serialVersionUID = 1L;
	
	private JPanel contentPane;
	private final JTextField taskNameTextFeild, descriptiontTextField;
	private ButtonsPanel taskButtons;
	private Event event;
	private final JButton createTaskButton;
	private JComboBox<Priority> comboBox;
	private final EventsManager manager;

	public TaskCreatorFrame(EventsManager manager, Event event, ButtonsPanel tasksButtons)
	{
		this.manager= manager;
		this.event= event;
		this.taskButtons= tasksButtons;
		setTitle("Task Creator");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 450, 670);
		contentPane = new JPanel();
		contentPane.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setBorder(new CompoundBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), new EmptyBorder(10, 10, 30, 10)));
		panel.setBounds(10, 71, 416, 430);
		contentPane.add(panel);
		panel.setLayout(new GridLayout(3, 1, 1, 40));
		
		JPanel namePanel = new JPanel();
		namePanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel.add(namePanel);
		
		JLabel nameLabel = new JLabel("Insert task name");
		nameLabel.setFont(new Font("Tahoma", Font.PLAIN, 18));
		namePanel.add(nameLabel);
		
		taskNameTextFeild = new JTextField();
		taskNameTextFeild.setFont(new Font("Tahoma", Font.PLAIN, 15));
		namePanel.add(taskNameTextFeild);
		taskNameTextFeild.setColumns(15);
		
		JPanel taskDescriptionPanel = new JPanel();
		taskDescriptionPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel.add(taskDescriptionPanel);
		
		JLabel lblNewLabel = new JLabel("Insert a task desription below");
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 18));
		taskDescriptionPanel.add(lblNewLabel);
		
		descriptiontTextField = new JTextField();
		descriptiontTextField.setFont(new Font("Tahoma", Font.PLAIN, 16));
		taskDescriptionPanel.add(descriptiontTextField);
		descriptiontTextField.setColumns(26);
		
		JPanel priorityPanel = new JPanel();
		priorityPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel.add(priorityPanel);
		
		JLabel priorityLabel = new JLabel("Select the priority of this event");
		priorityLabel.setFont(new Font("Tahoma", Font.PLAIN, 18));
		priorityPanel.add(priorityLabel);
		
		this.comboBox = new JComboBox<>();
		comboBox.setFont(new Font("Tahoma", Font.PLAIN, 16));
		comboBox.setModel(new DefaultComboBoxModel<>(Priority.values()));
		priorityPanel.add(comboBox);
		
		this.createTaskButton = new JButton("Create Task");
		createTaskButton.addActionListener(new Listener());
		createTaskButton.setFont(new Font("Tahoma", Font.PLAIN, 20));
		createTaskButton.setBounds(145, 570, 148, 38);
		contentPane.add(createTaskButton);
	}
	
	class Listener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			String name= taskNameTextFeild.getText();
			String description= descriptiontTextField.getText();
			Priority priority= (Priority) comboBox.getSelectedItem();
			event.addTask(name, description, priority);
			taskButtons.refresh(event.getAllTaskNamesSorted());
			manager.save();
			TaskCreatorFrame.this.dispose();
		}
	}
}

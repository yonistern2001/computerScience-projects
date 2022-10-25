package eventManager;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

public class TaskFrame extends JFrame
{
	private static final long serialVersionUID = 1L;
	
	private JPanel contentPane;
	private JTextField salaryTextField;
	private Task task;
	private JButton markCompleteButton;
	private JLabel alreadyCompleteLabel;
	private final EventsManager manager;
	private final JButton assignWorkerButton, deleteTaskButton;
	private JComboBox<Object> workerComboBox;
	private final Event event;
	private ButtonsPanel tasks;
	private DefaultListModel<String> listModel;

	public TaskFrame(Task task, EventsManager manager, Event event, ButtonsPanel tasks)
	{
		this.event= event;
		this.tasks= tasks;
		this.manager= manager;
		this.task= task;
		setTitle("Task Veiwer");
		setAlwaysOnTop(true);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 450, 670);
		contentPane = new JPanel();
		contentPane.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JPanel panel1 = new JPanel();
		panel1.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel1.setBounds(10, 33, 416, 269);
		contentPane.add(panel1);
		panel1.setLayout(null);
		
		JLabel nameLabel = new JLabel("Task Name: "+task.getName());
		nameLabel.setFont(new Font("Tahoma", Font.PLAIN, 18));
		nameLabel.setBounds(10, 10, 292, 28);
		panel1.add(nameLabel);
		
		Priority priority = task.getPriority();
		JLabel priorityLabel = new JLabel(priority.toString());
		priorityLabel.setBackground(Color.BLACK);
		priorityLabel.setForeground(getPriorityColor(priority));
		priorityLabel.setFont(new Font("Bookman Old Style", Font.BOLD, 22));
		priorityLabel.setBounds(287, 4, 119, 39);
		panel1.add(priorityLabel);
		
		
		this.alreadyCompleteLabel = new JLabel("Task Already Completed");
		alreadyCompleteLabel.setFont(new Font("Yu Gothic Medium", Font.PLAIN, 16));
		alreadyCompleteLabel.setBounds(109, 237, 191, 27);
		alreadyCompleteLabel.setVisible(false);
		panel1.add(alreadyCompleteLabel);
		
		this.markCompleteButton = new JButton("Mark Complete");
		markCompleteButton.addActionListener(new Listener());
		markCompleteButton.setFont(new Font("Tahoma", Font.PLAIN, 18));
		markCompleteButton.setBounds(10, 231, 162, 28);
		markCompleteButton.setVisible(false);
		panel1.add(markCompleteButton);
		
		JTextArea taskDescriptionTextFeild = new JTextArea();
		taskDescriptionTextFeild.setFont(new Font("Monospaced", Font.PLAIN, 16));
		taskDescriptionTextFeild.setText("Task Description: "+task.getTask());
		taskDescriptionTextFeild.setOpaque(false);
		taskDescriptionTextFeild.setLineWrap(true);
		taskDescriptionTextFeild.setEditable(false);
		taskDescriptionTextFeild.setBounds(10, 48, 396, 79);
		panel1.add(taskDescriptionTextFeild);
		
		this.listModel = new DefaultListModel<>();
		listModel.addAll(getWorkerAndSalarys());
		JList<String> list = new JList<>(listModel);
		list.setBorder(new CompoundBorder(new LineBorder(new Color(0, 0, 0)), new EmptyBorder(2, 2, 2, 2)));
		list.setForeground(Color.BLACK);
		list.setEnabled(false);
		list.setFont(new Font("Tahoma", Font.PLAIN, 16));
		list.setVisibleRowCount(8);
		list.setBounds(10, 137, 396, 84);
		panel1.add(list);
		
		JPanel assignWorkerPanel = new JPanel();
		assignWorkerPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		assignWorkerPanel.setBounds(10, 344, 416, 213);
		contentPane.add(assignWorkerPanel);
		assignWorkerPanel.setLayout(null);
		
		JLabel selectWorkerLabel = new JLabel("Select Worker:");
		selectWorkerLabel.setFont(new Font("Tahoma", Font.PLAIN, 18));
		selectWorkerLabel.setBounds(10, 22, 156, 33);
		assignWorkerPanel.add(selectWorkerLabel);
		
		this.assignWorkerButton = new JButton("Assign Worker to Task");
		assignWorkerButton.addActionListener(new Listener());
		assignWorkerButton.setFont(new Font("Tahoma", Font.PLAIN, 18));
		assignWorkerButton.setBounds(10, 163, 396, 28);
		assignWorkerPanel.add(assignWorkerButton);
		
		this.workerComboBox = new JComboBox<>(manager.getAllWorkers().toArray());
		workerComboBox.setFont(new Font("Tahoma", Font.PLAIN, 18));
		workerComboBox.setBounds(153, 22, 195, 30);
		assignWorkerPanel.add(workerComboBox);
		
		JLabel assignSalaryPanel = new JLabel("Set Worker Salary:");
		assignSalaryPanel.setFont(new Font("Tahoma", Font.PLAIN, 18));
		assignSalaryPanel.setBounds(10, 97, 156, 33);
		assignWorkerPanel.add(assignSalaryPanel);
		
		salaryTextField = new JTextField();
		salaryTextField.setFont(new Font("Tahoma", Font.PLAIN, 18));
		salaryTextField.setBounds(176, 98, 96, 28);
		assignWorkerPanel.add(salaryTextField);
		salaryTextField.setColumns(10);
		
		this.deleteTaskButton = new JButton("Delete");
		deleteTaskButton.setToolTipText("Delete task");
		deleteTaskButton.addActionListener(new Listener());
		deleteTaskButton.setForeground(Color.RED);
		deleteTaskButton.setFont(new Font("Tahoma", Font.BOLD, 20));
		deleteTaskButton.setBounds(160, 590, 105, 33);
		contentPane.add(deleteTaskButton);
		
		if(task.isComplete())
		{
			alreadyCompleteLabel.setVisible(true);
			assignWorkerButton.setEnabled(false);
			salaryTextField.setEnabled(false);
		}
		else
		{
			markCompleteButton.setVisible(true);
		}
	}

	private Collection<String> getWorkerAndSalarys()
	{
		List<String> output= new ArrayList<>();
		for(Worker worker: task.getWorkers())
		{
			String line= worker.getName()+"  "+task.getSalaryForWorkerAsString(worker);
			output.add(line);
		}
		return output;
	}


	private static Color getPriorityColor(Priority priority)
	{
		switch (priority)
		{
		case RED:
			return Color.RED;
		case YELLOW:
			return Color.YELLOW;
		default:
			return Color.GREEN;
		}
	}
	
	private class Listener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			Object source= e.getSource();
			if(source == markCompleteButton)
			{
				try
				{
					task.setCompleted();
					markCompleteButton.setVisible(false);
					alreadyCompleteLabel.setVisible(true);
					tasks.refresh(event.getAllTaskNamesSorted());
					assignWorkerButton.setEnabled(false);
					salaryTextField.setEnabled(false);
				}
				catch(IllegalArgumentException i)
				{
					JOptionPane.showMessageDialog(TaskFrame.this, "A worker must be assigned before being marked as complete","Error" , JOptionPane.ERROR_MESSAGE);;
				}
			}
			if(source == deleteTaskButton)
			{
				String name= task.getName();
				int result= JOptionPane.showConfirmDialog(rootPane, "Are you sure you want to delete "+name);
				if(result != JOptionPane.YES_OPTION)
					return;
				event.removeTask(name);
				tasks.refresh(event.getAllTaskNamesSorted());
				TaskFrame.this.dispose();
			}
			if(source == assignWorkerButton)
			{
				double salary= Double.parseDouble(salaryTextField.getText());
				Worker worker= (Worker) workerComboBox.getSelectedItem();
				task.assignWorker(worker, salary);
				salaryTextField.setText("");
				listModel.clear();
				listModel.addAll(getWorkerAndSalarys());
			}
			manager.save();
		}
	}
}
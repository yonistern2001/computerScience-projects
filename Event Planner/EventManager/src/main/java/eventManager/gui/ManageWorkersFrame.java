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
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.MatteBorder;

import eventManager.EventsManager;

public class ManageWorkersFrame extends JFrame
{
	private static final long serialVersionUID = 1L;
	
	private JPanel contentPane;
	private final JTextField name, email, number, workerToDelete;
	private final JButton deleteWorkerButton, addWorkerButton;
	private final EventsManager manager;
	private JLabel errorLabel;

	public ManageWorkersFrame(EventsManager manager)
	{
		this.manager= manager;
		setAlwaysOnTop(true);
		setTitle("Worker Manager");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 450, 670);
		contentPane = new JPanel();
		contentPane.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JPanel addWorkerPanel = new JPanel();
		addWorkerPanel.setBorder(new MatteBorder(1, 1, 1, 1, (Color) new Color(0, 0, 0)));
		addWorkerPanel.setBounds(10, 28, 416, 308);
		contentPane.add(addWorkerPanel);
		addWorkerPanel.setLayout(null);
		
		JLabel workersNameLabel = new JLabel("Insert workers name:");
		workersNameLabel.setFont(new Font("Tahoma", Font.PLAIN, 18));
		workersNameLabel.setBounds(10, 24, 176, 31);
		addWorkerPanel.add(workersNameLabel);
		
		name = new JTextField();
		name.setFont(new Font("Tahoma", Font.PLAIN, 13));
		name.setBounds(196, 24, 181, 26);
		addWorkerPanel.add(name);
		name.setColumns(10);
		
		JLabel workersEmailLabel = new JLabel("Insert workers email:");
		workersEmailLabel.setFont(new Font("Tahoma", Font.PLAIN, 18));
		workersEmailLabel.setBounds(10, 83, 176, 31);
		addWorkerPanel.add(workersEmailLabel);
		
		email = new JTextField();
		email.setFont(new Font("Tahoma", Font.PLAIN, 13));
		email.setColumns(10);
		email.setBounds(196, 83, 181, 26);
		addWorkerPanel.add(email);
		
		JLabel workersNumberPanel = new JLabel("Insert workers phone number below:");
		workersNumberPanel.setFont(new Font("Tahoma", Font.PLAIN, 18));
		workersNumberPanel.setBounds(10, 141, 299, 31);
		addWorkerPanel.add(workersNumberPanel);
		
		number = new JTextField();
		number.setFont(new Font("Tahoma", Font.PLAIN, 13));
		number.setColumns(10);
		number.setBounds(67, 182, 189, 24);
		addWorkerPanel.add(number);
		
		this.addWorkerButton = new JButton("Add Worker");
		addWorkerButton.addActionListener(new Listener());
		addWorkerButton.setFont(new Font("Tahoma", Font.PLAIN, 18));
		addWorkerButton.setBounds(133, 251, 140, 31);
		addWorkerPanel.add(addWorkerButton);
		
		JPanel deleteWorkerPanel = new JPanel();
		deleteWorkerPanel.setBorder(new MatteBorder(1, 1, 1, 1, (Color) new Color(0, 0, 0)));
		deleteWorkerPanel.setBounds(10, 379, 416, 203);
		contentPane.add(deleteWorkerPanel);
		deleteWorkerPanel.setLayout(null);
		
		JLabel deleteWorkerLabel = new JLabel("Insert worker to delete below:");
		deleteWorkerLabel.setBounds(64, 10, 278, 25);
		deleteWorkerLabel.setFont(new Font("Tahoma", Font.PLAIN, 20));
		deleteWorkerPanel.add(deleteWorkerLabel);
		
		workerToDelete = new JTextField();
		workerToDelete.setFont(new Font("Tahoma", Font.PLAIN, 13));
		workerToDelete.setBounds(105, 58, 205, 25);
		deleteWorkerPanel.add(workerToDelete);
		workerToDelete.setColumns(10);
		
		this.deleteWorkerButton = new JButton("Delete Worker");
		deleteWorkerButton.addActionListener(new Listener());
		deleteWorkerButton.setForeground(Color.RED);
		deleteWorkerButton.setFont(new Font("Tahoma", Font.BOLD, 18));
		deleteWorkerButton.setBounds(116, 129, 181, 33);
		deleteWorkerPanel.add(deleteWorkerButton);
		
		this.errorLabel = new JLabel("The worker you entered does not exist");
		errorLabel.setForeground(Color.RED);
		errorLabel.setFont(new Font("Tahoma", Font.PLAIN, 14));
		errorLabel.setVisible(false);
		errorLabel.setBounds(88, 93, 254, 26);
		deleteWorkerPanel.add(errorLabel);
	}
	
	class Listener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			Object source= e.getSource();
			if(source == deleteWorkerButton)
			{
				errorLabel.setVisible(false);
				String name= workerToDelete.getText();
				int result= JOptionPane.showConfirmDialog(rootPane, "Are you sure you want to delete "+name);
				if(result != JOptionPane.YES_OPTION)
					return;
				if(manager.hasWorker(name))
				{
					manager.removeWorker(name);
					manager.save();
				}
				else
				{
					errorLabel.setVisible(true);
				}
				workerToDelete.setText("");
			}
			if(source == addWorkerButton)
			{
				String name= ManageWorkersFrame.this.name.getText();
				String number= ManageWorkersFrame.this.number.getText();
				String email= ManageWorkersFrame.this.email.getText();
				manager.addWorker(name, number, email);
				ManageWorkersFrame.this.name.setText("");
				ManageWorkersFrame.this.email.setText("");
				ManageWorkersFrame.this.number.setText("");
				manager.save();
			}
		}
	}
}
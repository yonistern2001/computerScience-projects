package eventManager;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class ViewWorkersFrame extends JFrame
{
	private static final long serialVersionUID = 1L;
	
	private JPanel contentPane;
	private final JLabel emailPanel, numberPanel, nameLabel;
	private JList<Worker> list;

	public ViewWorkersFrame(EventsManager manager)
	{
		setTitle("Workers Veiwer");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 450, 670);
		contentPane = new JPanel();
		contentPane.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel workersLabel = new JLabel("Workers");
		workersLabel.setFont(new Font("Tahoma", Font.PLAIN, 22));
		workersLabel.setBounds(38, 50, 99, 39);
		contentPane.add(workersLabel);
		
		JPanel panel = new JPanel();
		panel.setBorder(new CompoundBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), new EmptyBorder(10, 10, 25, 10)));
		panel.setBounds(10, 436, 416, 175);
		contentPane.add(panel);
		panel.setLayout(new GridLayout(3, 0, 0, 35));
		
		this.nameLabel = new JLabel("Worker Name: ");
		nameLabel.setFont(new Font("Tahoma", Font.PLAIN, 18));
		panel.add(nameLabel);
		
		this.numberPanel = new JLabel("Phone Number: ");
		numberPanel.setFont(new Font("Tahoma", Font.PLAIN, 18));
		panel.add(numberPanel);
		
		this.emailPanel = new JLabel("Email: ");
		emailPanel.setFont(new Font("Tahoma", Font.PLAIN, 18));
		panel.add(emailPanel);
		
		DefaultListModel<Worker> listModel= new DefaultListModel<>();
		listModel.addAll(manager.getAllWorkers());
		this.list = new JList<>(listModel);
		list.setBackground(Color.WHITE);
		list.setBorder(new CompoundBorder(new LineBorder(new Color(64, 64, 64), 1, true), new EmptyBorder(5, 5, 5, 5)));
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setFont(new Font("Tahoma", Font.PLAIN, 18));
		list.setBounds(38, 123, 340, 272);
		list.addListSelectionListener(new Listener());
		contentPane.add(list);
	}
	
	private class Listener implements ListSelectionListener
	{
		@Override
		public void valueChanged(ListSelectionEvent e)
		{
			Worker worker= list.getSelectedValue();
			nameLabel.setText("Worker Name: "+worker.getName());
			numberPanel.setText("Phone Number: "+worker.getPhoneNumber());
			emailPanel.setText("Email: "+worker.getEmail());
		}
	}
}
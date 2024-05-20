package eventManager.gui;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

import eventManager.Event;
import eventManager.EventsManager;

public class MainFrame extends JFrame
{
	private static final long serialVersionUID = 1L;
	
	private JPanel contentPane;
	private final JButton btnManageWorkers, AddEventButton;
	private final EventsManager manager;
	private ButtonsPanel events;
	private JLabel SelectEventLabel;
	private JButton veiwWorkersButton;
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainFrame frame = new MainFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public MainFrame()
	{
		setTitle("Events Manager");
		this.manager= new EventsManager();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 670);
		contentPane = new JPanel();
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel.setBounds(27, 33, 340, 333);
		contentPane.add(panel);
		panel.setLayout(null);
		
		events = new ButtonsPanel(manager.getAllEventNamesSorted(), new ButtonListener());
		events.setBounds(22, 42, 287, 269);
		panel.add(events);
		
		SelectEventLabel = new JLabel("Select an event");
		SelectEventLabel.setHorizontalAlignment(SwingConstants.LEFT);
		SelectEventLabel.setFont(new Font("Tahoma", Font.PLAIN, 20));
		SelectEventLabel.setBounds(22, 10, 152, 22);
		panel.add(SelectEventLabel);
		
		this.AddEventButton = new JButton("Add Event");
		AddEventButton.addActionListener(new Listener());
		AddEventButton.setFont(new Font("Tahoma", Font.PLAIN, 18));
		AddEventButton.setBounds(27, 409, 137, 38);
		contentPane.add(AddEventButton);
		
		this.btnManageWorkers = new JButton("Manage Workers");
		btnManageWorkers.addActionListener(new Listener());
		btnManageWorkers.setFont(new Font("Tahoma", Font.PLAIN, 18));
		btnManageWorkers.setBounds(27, 468, 180, 38);
		contentPane.add(btnManageWorkers);
		
		veiwWorkersButton = new JButton("Veiw All Workers");
		veiwWorkersButton.addActionListener(new Listener());
		veiwWorkersButton.setFont(new Font("Tahoma", Font.PLAIN, 18));
		veiwWorkersButton.setBounds(27, 531, 180, 38);
		contentPane.add(veiwWorkersButton);
	}

	class Listener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			Object source = e.getSource();
			JFrame frame = null;
			if (source == btnManageWorkers)
			{
				frame = new ManageWorkersFrame(manager);
			}
			if (source == AddEventButton)
			{
				frame = new EventCreatorFrame(manager, events);
			}
			if(source == veiwWorkersButton)
			{
				frame= new ViewWorkersFrame(manager);
			}
			frame.pack();
			frame.setSize(450, 670);
			frame.setVisible(true);
		}
	}
	
	private class ButtonListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			JButton source = (JButton) e.getSource();
			String eventName= source.getText();
			Event event= manager.getEventByName(eventName);
			System.out.println(event.getEventName()+" "+event.getLocation()+" "+event.getTimeAndDate().toString());
			JFrame frame= new EventFrame(event, manager, events);
			frame.pack();
			frame.setSize(450, 670);
			frame.setVisible(true);
		}
	}
}
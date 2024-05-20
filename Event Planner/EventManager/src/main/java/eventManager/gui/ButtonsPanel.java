package eventManager.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

public class ButtonsPanel extends JPanel
{
	private static final long serialVersionUID = 1L;
	
	private ActionListener listener;
	
	public ButtonsPanel(List<String> words, ActionListener listener)
	{
		this.setBackground(Color.WHITE);
		this.listener= listener;
		this.setPreferredSize(new Dimension(200, 200));
		Border lineBorder = BorderFactory.createLineBorder(Color.BLACK, 1);
		Border emptyBorder= new EmptyBorder(4, 5, 4, 5);
		this.setBorder(new CompoundBorder(lineBorder, emptyBorder));
		this.setLayout(new GridLayout(6, 1, 0, 3));
		createButtons(words);
	}

	private void createButtons(List<String> words)
	{
		for(String word: words)
		{
			this.add(createButton(word, 20, this.listener, SwingConstants.LEFT));
		}
	}
	
	protected void refresh(List<String> words)
	{
		deleteButtons();
		createButtons(words);
		this.revalidate();
		this.repaint();
	}
	
	private void deleteButtons()
	{
		this.removeAll();
	}

	protected static JButton createButton(String buttonName, int size, ActionListener listener, int alignment)
	{	
		JButton button= new JButton(buttonName);
		button.setBackground(new Color(248, 248, 249));
		button.addActionListener(listener);
		button.setHorizontalAlignment(alignment);
		button.setFont(new Font(Font.DIALOG, Font.PLAIN, size));
		button.setFocusPainted(false);
		return button;
	}
}
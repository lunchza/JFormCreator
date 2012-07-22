/**
 * @author Peter Pretorius
 * This is a simple program that I wrote to emulate the "status bar" panel inherently
 * present in other languages and windows in particular. The status is delivered by means
 * of a JLabel.
 */

package jTools;

import java.awt.Color;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class JStatusBar extends JPanel {
	private JLabel message;
	public static final int LEFT_ORIENTATION = 0;
	public static final int RIGHT_ORIENTATION = 1;
	
	public JStatusBar(int orientation)
	{
		setOrientation(orientation);
		setBorder(BorderFactory.createLineBorder(Color.BLACK));
		message = new JLabel("");
		add(message);
	}
	
	public JStatusBar(String m, int orientation)
	{
		setOrientation(orientation);
		setBorder(BorderFactory.createLineBorder(Color.BLACK));
		message = new JLabel(m);
		add(message);
	}
	
	public void setStatus(String s)
	{
		message.setText(s);
	}
	
	public void setOrientation(int orientation)
	{
		switch(orientation)
		{
		case LEFT_ORIENTATION:
			setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			break;
		case RIGHT_ORIENTATION:
			setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
			break;
		}
	}

}
